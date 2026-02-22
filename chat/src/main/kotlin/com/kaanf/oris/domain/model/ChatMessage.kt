package com.kaanf.oris.domain.model

import com.kaanf.oris.domain.type.ChatId
import com.kaanf.oris.domain.type.ChatMessageId
import java.time.Instant
import java.time.OffsetDateTime

data class ChatMessage(
    val id: ChatMessageId,
    val chatId: ChatId,
    val sender: ChatParticipant,
    val content: String,
    val createdAt: Instant,
)
