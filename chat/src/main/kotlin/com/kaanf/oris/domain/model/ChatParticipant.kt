package com.kaanf.oris.domain.model

import com.kaanf.oris.domain.type.UserId

data class ChatParticipant(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
)
