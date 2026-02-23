package com.kaanf.oris.domain.model

import com.kaanf.oris.domain.type.UserId
import java.time.Instant

data class DeviceToken(
    val id: Long,
    val userId: UserId,
    val token: String,
    val platform: Platform,
    val createdAt: Instant = Instant.now(),
) {
    enum class Platform {
        ANDROID, IOS
    }
}