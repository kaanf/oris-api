package com.kaanf.oris.infra.db.mapper

import com.kaanf.oris.domain.model.User
import com.kaanf.oris.infra.db.entity.UserEntity

fun UserEntity.toUser(): User {
    return User(
        id = id!!,
        username = username,
        email = email,
        hasVerifiedEmail = hasVerifiedEmail,
    )
}