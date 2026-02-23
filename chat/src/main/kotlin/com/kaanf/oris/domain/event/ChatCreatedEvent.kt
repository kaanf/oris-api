package com.kaanf.oris.domain.event

import com.kaanf.oris.domain.type.ChatId
import com.kaanf.oris.domain.type.UserId

data class ChatCreatedEvent(
    val chatId: ChatId,
    val participantIds: List<UserId>
)
