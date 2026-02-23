package com.kaanf.oris.domain.model

import com.kaanf.oris.domain.type.UserId

data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val hasVerifiedEmail: Boolean
)