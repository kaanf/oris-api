package com.kaanf.oris.api.dto

import com.kaanf.oris.domain.type.UserId

data class ChatParticipantDto(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
)
