package com.kaanf.oris.api.controllers

import com.kaanf.oris.api.dto.DeviceTokenDto
import com.kaanf.oris.api.dto.RegisterDeviceRequest
import com.kaanf.oris.api.mappers.toDeviceTokenDto
import com.kaanf.oris.api.mappers.toPlatformDto
import com.kaanf.oris.api.util.requestUserId
import com.kaanf.oris.service.PushNotificationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/notification")
class DeviceTokenController(
    private val pushNotificationService: PushNotificationService
) {
    @PostMapping("/register")
    fun registerDeviceToken(
        @Valid @RequestBody body: RegisterDeviceRequest,
    ): DeviceTokenDto {
        return pushNotificationService.registerDevice(
            userId = requestUserId,
            token = body.token,
            platform = body.platform.toPlatformDto()
        ).toDeviceTokenDto()
    }

    @DeleteMapping("/{token}")
    fun unregisterDeviceToken(
        @PathVariable("token") token: String,
    ) {
        pushNotificationService.unregisterDevice(token)
    }
}