package com.kaanf.oris.service

import com.kaanf.oris.api.dto.ChatMessageDto
import com.kaanf.oris.api.mapper.toChatMessageDto
import com.kaanf.oris.domain.event.ChatCreatedEvent
import com.kaanf.oris.domain.event.ChatParticipantLeftEvent
import com.kaanf.oris.domain.event.ChatParticipantsJoinedEvent
import com.kaanf.oris.domain.exception.ChatNotFoundException
import com.kaanf.oris.domain.exception.ChatParticipantNotFoundException
import com.kaanf.oris.domain.exception.ForbiddenException
import com.kaanf.oris.domain.exception.InvalidChatSizeException
import com.kaanf.oris.domain.model.Chat
import com.kaanf.oris.domain.model.ChatMessage
import com.kaanf.oris.domain.type.ChatId
import com.kaanf.oris.domain.type.UserId
import com.kaanf.oris.infra.db.entity.ChatEntity
import com.kaanf.oris.infra.db.mapper.toChat
import com.kaanf.oris.infra.db.mapper.toChatMessage
import com.kaanf.oris.infra.db.repository.ChatMessageRepository
import com.kaanf.oris.infra.db.repository.ChatParticipantRepository
import com.kaanf.oris.infra.db.repository.ChatRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @Cacheable(
        value = ["messages"],
        key = "#chatId",
        condition = "#before == null && #pageSize <= 50",
        sync = true
    )
    fun getChatMessages(chatId: ChatId, before: Instant?, pageSize: Int): List<ChatMessageDto> {
        return chatMessageRepository
            .findByChatIdBefore(
                chatId = chatId,
                before = before ?: Instant.now(),
                pageable = PageRequest.of(0, pageSize)
            )
            .content
            .asReversed()
            .map { it.toChatMessage().toChatMessageDto() }
    }

    fun getChatById(chatId: ChatId, requestUserId: UserId): Chat? {
        return chatRepository
            .findChatById(chatId, requestUserId)
            ?.toChat(lastMessageForChat(chatId))
    }

    fun findChatsByUser(userId: UserId): List<Chat> {
        val chatEntities = chatRepository.findAllByUserId(userId)

        val chatIds = chatEntities.mapNotNull { it.id }

        val latestMessages = chatMessageRepository
            .findLatestMessagesByChatIds(chatIds.toSet())
            .associateBy { it.chatId }

        return chatEntities.map {
            it.toChat(
                lastMessage = latestMessages[it.id]?.toChatMessage(),
            )
        }.sortedByDescending { it.lastActivityAt }
    }

    @Transactional
    fun createChat(creatorId: UserId, otherUserIds: Set<UserId>): Chat {
        val otherParticipants = chatParticipantRepository.findByUserIdIn(otherUserIds)
        val allParticipants = otherParticipants + creatorId

        if (allParticipants.size < 2) {
            throw InvalidChatSizeException()
        }

        val creator = chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundException(creatorId)

        return chatRepository.saveAndFlush(
            ChatEntity(
                creator = creator,
                participants = setOf(creator) + otherParticipants
            )
        ).toChat(lastMessage = null).also { entity ->
            applicationEventPublisher.publishEvent(
                ChatCreatedEvent(
                    chatId = entity.id,
                    participantIds = entity.participants.map { it.userId }
                )
            )
        }
    }

    @Transactional
    fun addParticipants(
        requestUserId: UserId,
        chatId: ChatId,
        userIds: Set<UserId>
    ): Chat {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()

        val isRequestingUserInChat = chat.participants.any {
            it.userId == requestUserId
        }

        if (!isRequestingUserInChat) {
            throw ForbiddenException()
        }

        val users = userIds.map { userId ->
            chatParticipantRepository.findByIdOrNull(userId)
                ?: throw ChatParticipantNotFoundException(userId)
        }

        val lastMessage = lastMessageForChat(chatId)
        val updatedChat = chatRepository.save(
            chat.apply {
                this.participants = chat.participants + users
            }
        ).toChat(lastMessage)

        applicationEventPublisher.publishEvent(
            ChatParticipantsJoinedEvent(
                chatId = chatId,
                userIds = userIds,
            )
        )

        return updatedChat
    }

    @Transactional
    fun removeParticipant(chatId: ChatId, userId: UserId) {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()

        val participant = chat.participants.find { it.userId == userId }
            ?: throw ChatParticipantNotFoundException(userId)

        val newParticipantsSize = chat.participants.size - 1

        if (newParticipantsSize == 0) {
            chatRepository.deleteById(chatId)
            return
        }

        chatRepository.save(
            chat.apply {
                this.participants = chat.participants - participant
            }
        )

        applicationEventPublisher.publishEvent(
            ChatParticipantLeftEvent(
                chatId = chatId,
                userId = userId,
            )
        )
    }

    private fun lastMessageForChat(chatId: ChatId): ChatMessage? {
        return chatMessageRepository
            .findLatestMessagesByChatIds(setOf(chatId))
            .firstOrNull()
            ?.toChatMessage()
    }
}