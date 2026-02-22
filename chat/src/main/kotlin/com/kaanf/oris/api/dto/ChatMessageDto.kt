package com.kaanf.oris.api.dto

import com.kaanf.oris.domain.type.ChatId
import com.kaanf.oris.domain.type.ChatMessageId
import com.kaanf.oris.domain.type.UserId
import java.time.Instant

data class ChatMessageDto(
    val id: ChatMessageId,
    val chatId: ChatId,
    val content: String,
    val createdAt: Instant,
    val senderId: UserId
)