package com.kaanf.oris.api.dto

import com.kaanf.oris.domain.type.UserId
import java.time.Instant

data class DeviceTokenDto(
    val userId: UserId,
    val token: String,
    val createdAt: Instant
)