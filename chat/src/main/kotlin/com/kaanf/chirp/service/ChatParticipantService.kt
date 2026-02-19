package com.kaanf.chirp.service

import com.kaanf.chirp.domain.model.Chat
import com.kaanf.chirp.domain.model.ChatParticipant
import com.kaanf.chirp.domain.type.UserId
import com.kaanf.chirp.infra.db.mapper.toChatParticipant
import com.kaanf.chirp.infra.db.mapper.toChatParticipantEntity
import com.kaanf.chirp.infra.db.repository.ChatParticipantRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ChatParticipantService(
    private val chatParticipantRepository: ChatParticipantRepository
) {
    fun createChatParticipant(chatParticipant: ChatParticipant) {
        chatParticipantRepository.save(
            chatParticipant.toChatParticipantEntity()
        )
    }

    fun findChatParticipantById(userId: UserId): ChatParticipant? {
        return chatParticipantRepository.findByIdOrNull(userId)?.toChatParticipant()
    }

    fun findChatParticipantByEmailOrUsername(query: String): ChatParticipant? {
        val normalizedQuery = query.lowercase().trim()
        return chatParticipantRepository.findByEmailOrUsername(
            query = normalizedQuery
        )?.toChatParticipant()
    }
}