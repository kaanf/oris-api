package com.kaanf.oris.domain.model

import com.kaanf.oris.domain.type.ChatId
import java.time.Instant

data class Chat(
    val id: ChatId,
    val participants: Set<ChatParticipant>,
    val lastMessage: ChatMessage?,
    val creator: ChatParticipant,
    val lastActivityAt : Instant,
    val createdAt: Instant
)