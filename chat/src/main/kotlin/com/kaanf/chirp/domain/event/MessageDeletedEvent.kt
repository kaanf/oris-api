package com.kaanf.chirp.domain.event

import com.kaanf.chirp.domain.type.ChatId
import com.kaanf.chirp.domain.type.ChatMessageId

data class MessageDeletedEvent(
    val chatId: ChatId,
    val messageId: ChatMessageId,
)