package com.kaanf.chirp.domain.model

import com.kaanf.chirp.domain.type.UserId

data class ChatParticipant(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
)
