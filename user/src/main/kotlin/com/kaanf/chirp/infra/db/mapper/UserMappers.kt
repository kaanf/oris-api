package com.kaanf.chirp.infra.db.mapper

import com.kaanf.chirp.domain.model.User
import com.kaanf.chirp.infra.db.entity.UserEntity

fun UserEntity.toUser(): User {
    return User(
        id = id!!,
        username = username,
        email = email,
        hasVerifiedEmail = hasVerifiedEmail,
    )
}