package com.kaanf.chirp.domain.model

import com.kaanf.chirp.domain.type.UserId

data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val hasVerifiedEmail: Boolean
)