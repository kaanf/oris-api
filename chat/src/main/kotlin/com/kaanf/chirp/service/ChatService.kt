package com.kaanf.chirp.service

import com.kaanf.chirp.domain.exception.ChatNotFoundException
import com.kaanf.chirp.domain.exception.ChatParticipantNotFoundException
import com.kaanf.chirp.domain.exception.ForbiddenException
import com.kaanf.chirp.domain.exception.InvalidChatSizeException
import com.kaanf.chirp.domain.model.Chat
import com.kaanf.chirp.domain.model.ChatMessage
import com.kaanf.chirp.domain.type.ChatId
import com.kaanf.chirp.domain.type.UserId
import com.kaanf.chirp.infra.db.entity.ChatEntity
import com.kaanf.chirp.infra.db.mapper.toChat
import com.kaanf.chirp.infra.db.mapper.toChatMessage
import com.kaanf.chirp.infra.db.repository.ChatMessageRepository
import com.kaanf.chirp.infra.db.repository.ChatParticipantRepository
import com.kaanf.chirp.infra.db.repository.ChatRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository
) {
    @Transactional
    fun createChat(creatorId: UserId, otherUserIds: Set<UserId>): Chat {
        val otherParticipants = chatParticipantRepository.findByUserIdIn(otherUserIds)
        val allParticipants = otherParticipants + creatorId

        if (allParticipants.size < 2) {
            throw InvalidChatSizeException()
        }

        val creator = chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundException(creatorId)

        return chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = setOf(creator) + otherParticipants
            )
        ).toChat(lastMessage = null)
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
    }

    private fun lastMessageForChat(chatId: ChatId): ChatMessage? {
        return chatMessageRepository
            .findLatestMessagesByChatIds(setOf(chatId))
            .firstOrNull()
            ?.toChatMessage()
    }
}