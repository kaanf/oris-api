package com.kaanf.oris.api.dto

import com.kaanf.oris.domain.type.UserId

data class UserDto(
    val id: UserId,
    val email: String,
    val username: String,
    val hasVerifiedEmail: Boolean,
)