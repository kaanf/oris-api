package com.kaanf.chirp.domain.model

import com.kaanf.chirp.domain.type.ChatId
import com.kaanf.chirp.domain.type.ChatMessageId
import java.time.Instant
import java.time.OffsetDateTime

data class ChatMessage(
    val id: ChatMessageId,
    val chatId: ChatId,
    val sender: ChatParticipant,
    val content: String,
    val createdAt: Instant,
)
