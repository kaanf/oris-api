package com.kaanf.chirp.domain.event

import com.kaanf.chirp.domain.type.ChatId
import com.kaanf.chirp.domain.type.UserId

data class ChatParticipantLeftEvent(
    val chatId: ChatId,
    val userId: UserId,
)