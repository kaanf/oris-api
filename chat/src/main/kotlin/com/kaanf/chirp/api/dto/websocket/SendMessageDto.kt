package com.kaanf.chirp.api.dto.websocket

import com.kaanf.chirp.domain.type.ChatId
import com.kaanf.chirp.domain.type.ChatMessageId

data class SendMessageDto(
    val chatId: ChatId,
    val content: String,
    val messageId: ChatMessageId? = null
)