package com.kaanf.chirp.service

import com.kaanf.chirp.api.dto.ChatMessageDto
import com.kaanf.chirp.api.mapper.toChatMessageDto
import com.kaanf.chirp.domain.event.MessageDeletedEvent
import com.kaanf.chirp.domain.event.chat.ChatEvent
import com.kaanf.chirp.domain.exception.ChatNotFoundException
import com.kaanf.chirp.domain.exception.ChatParticipantNotFoundException
import com.kaanf.chirp.domain.exception.ForbiddenException
import com.kaanf.chirp.domain.exception.MessageNotFoundException
import com.kaanf.chirp.domain.model.ChatMessage
import com.kaanf.chirp.domain.type.ChatId
import com.kaanf.chirp.domain.type.ChatMessageId
import com.kaanf.chirp.domain.type.UserId
import com.kaanf.chirp.infra.db.entity.ChatMessageEntity
import com.kaanf.chirp.infra.db.mapper.toChatMessage
import com.kaanf.chirp.infra.db.repository.ChatMessageRepository
import com.kaanf.chirp.infra.db.repository.ChatParticipantRepository
import com.kaanf.chirp.infra.db.repository.ChatRepository
import com.kaanf.chirp.infra.message_queue.EventPublisher
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val eventPublisher: EventPublisher
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
                id = messageId,
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

        evictMessagesCache(message.chatId)
    }


    @CacheEvict(
        value = ["messages"],
        key = "#chatId"
    )
    fun evictMessagesCache(chatId: ChatId) {
        // NO-OP, let Spring handle the cache evict.
    }
}