package com.kaanf.chirp.domain.event

import com.kaanf.chirp.domain.type.ChatId
import com.kaanf.chirp.domain.type.UserId

data class ChatParticipantsJoinedEvent(
    val chatId: ChatId,
    val userIds: Set<UserId>,
)