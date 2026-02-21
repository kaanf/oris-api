package com.kaanf.oris.api.dto.websocket

import com.kaanf.oris.domain.type.ChatId
import com.kaanf.oris.domain.type.ChatMessageId

data class SendMessageDto(
    val chatId: ChatId,
    val content: String,
    val messageId: ChatMessageId? = null
)