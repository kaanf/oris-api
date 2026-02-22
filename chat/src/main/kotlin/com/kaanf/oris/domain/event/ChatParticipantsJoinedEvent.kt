package com.kaanf.oris.domain.event

import com.kaanf.oris.domain.type.ChatId
import com.kaanf.oris.domain.type.UserId

data class ChatParticipantsJoinedEvent(
    val chatId: ChatId,
    val userIds: Set<UserId>,
)