package com.kaanf.chirp.api.dto

import com.kaanf.chirp.domain.type.UserId

data class UserDto(
    val id: UserId,
    val email: String,
    val username: String,
    val hasVerifiedEmail: Boolean,
)