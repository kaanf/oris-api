package com.kaanf.chirp.api.dto

import com.kaanf.chirp.domain.type.ChatId
import com.kaanf.chirp.domain.type.ChatMessageId
import com.kaanf.chirp.domain.type.UserId
import java.time.Instant

data class ChatMessageDto(
    val id: ChatMessageId,
    val chatId: ChatId,
    val content: String,
    val createdAt: Instant,
    val senderId: UserId
)