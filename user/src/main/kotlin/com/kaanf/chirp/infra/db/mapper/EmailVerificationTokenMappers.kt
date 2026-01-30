package com.kaanf.chirp.infra.db.mapper

import com.kaanf.chirp.domain.model.EmailVerificationToken
import com.kaanf.chirp.infra.db.entity.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken(): EmailVerificationToken {
    return EmailVerificationToken(
        id = id,
        token = token,
        user = user.toUser()
    )
}