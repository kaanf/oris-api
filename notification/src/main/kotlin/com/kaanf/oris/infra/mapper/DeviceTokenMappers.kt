package com.kaanf.oris.infra.mapper

import com.kaanf.oris.domain.model.DeviceToken
import com.kaanf.oris.infra.db.DeviceTokenEntity

fun DeviceTokenEntity.toDeviceToken(): DeviceToken {
    return DeviceToken(
        userId = userId,
        token = token,
        platform = platform.toPlatform(),
        createdAt = createdAt,
        id = id
    )
}