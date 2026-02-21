package com.kaanf.oris.api.util

import com.kaanf.oris.domain.exception.UnauthorizedException
import com.kaanf.oris.domain.type.UserId
import org.springframework.security.core.context.SecurityContextHolder

val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId
        ?: throw UnauthorizedException()