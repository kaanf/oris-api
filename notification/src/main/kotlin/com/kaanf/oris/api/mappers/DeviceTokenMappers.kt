package com.kaanf.oris.api.mappers

import com.kaanf.oris.api.dto.DeviceTokenDto
import com.kaanf.oris.api.dto.PlatformDto
import com.kaanf.oris.domain.model.DeviceToken

fun DeviceToken.toDeviceTokenDto(): DeviceTokenDto {
    return DeviceTokenDto(
        userId = userId,
        token = token,
        createdAt = createdAt,
    )
}

fun PlatformDto.toPlatformDto(): DeviceToken.Platform {
    return when (this) {
        PlatformDto.ANDROID -> DeviceToken.Platform.ANDROID
        PlatformDto.IOS -> DeviceToken.Platform.IOS
    }
}