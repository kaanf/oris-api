package com.kaanf.oris.service

import com.kaanf.oris.domain.event.MessageDeletedEvent
import com.kaanf.oris.domain.event.chat.ChatEvent
import com.kaanf.oris.domain.exception.ChatNotFoundException
import com.kaanf.oris.domain.exception.ChatParticipantNotFoundException
import com.kaanf.oris.domain.exception.ForbiddenException
import com.kaanf.oris.domain.exception.MessageNotFoundException
import com.kaanf.oris.domain.model.ChatMessage
import com.kaanf.oris.domain.type.ChatId
import com.kaanf.oris.domain.type.ChatMessageId
import com.kaanf.oris.domain.type.UserId
import com.kaanf.oris.infra.db.entity.ChatMessageEntity
import com.kaanf.oris.infra.db.mapper.toChatMessage
import com.kaanf.oris.infra.db.repository.ChatMessageRepository
import com.kaanf.oris.infra.db.repository.ChatParticipantRepository
import com.kaanf.oris.infra.db.repository.ChatRepository
import com.kaanf.oris.infra.message_queue.EventPublisher
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val eventPublisher: EventPublisher,
    private val messageCache: MessageCacheEvictionHelper,
) {
    @Transactional
    @CacheEvict(
        value = ["messages"],
        key = "#chatId"
    )
    fun sendMessage(
        chatId: ChatId,
        senderId: UserId,
        content: String,
        messageId: ChatMessageId? = null
    ): ChatMessage {
        val chat = chatRepository.findChatById(chatId, senderId)
            ?: throw ChatNotFoundException()

        val sender = chatParticipantRepository.findByIdOrNull(senderId)
            ?: throw ChatParticipantNotFoundException(senderId)

        val savedMessage = chatMessageRepository.saveAndFlush(
            ChatMessageEntity(
                id = messageId ?: UUID.randomUUID(),
                content = content.trim(),
                chatId = chatId,
                chat = chat,
                sender = sender,
            )
        )

        eventPublisher.publish(
            event = ChatEvent.NewMessage(
                senderId = sender.userId,
                senderUsername = sender.username,
                recipientIds = chat.participants.map { it.userId }.toSet(),
                chatId = chatId,
                message = savedMessage.content
            )
        )

        return savedMessage.toChatMessage()
    }

    @Transactional
    fun deleteMessage(requestUserId: UserId, messageId: ChatMessageId){
        val message = chatMessageRepository.findByIdOrNull(messageId)
            ?: throw MessageNotFoundException(messageId)

        if (message.sender.userId != requestUserId) {
            throw ForbiddenException()
        }

        chatMessageRepository.delete(message)

        applicationEventPublisher.publishEvent(
            MessageDeletedEvent(
                chatId = message.chatId,
                messageId = messageId
            )
        )

        messageCache.evictMessagesCache(message.chatId)
    }
}