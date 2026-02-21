package com.kaanf.oris.domain.event

import com.kaanf.oris.domain.type.ChatId
import com.kaanf.oris.domain.type.ChatMessageId

data class MessageDeletedEvent(
    val chatId: ChatId,
    val messageId: ChatMessageId,
)