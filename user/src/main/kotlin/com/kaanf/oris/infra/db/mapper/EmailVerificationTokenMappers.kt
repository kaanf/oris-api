package com.kaanf.oris.infra.db.mapper

import com.kaanf.oris.domain.model.EmailVerificationToken
import com.kaanf.oris.infra.db.entity.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken(): EmailVerificationToken {
    return EmailVerificationToken(
        id = id,
        token = token,
        user = user.toUser()
    )
}