package com.kaanf.chirp.api.util

import com.kaanf.chirp.domain.exception.UnauthorizedException
import com.kaanf.chirp.domain.model.UserId
import org.springframework.security.core.context.SecurityContextHolder

val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId
        ?: throw UnauthorizedException()