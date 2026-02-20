package com.kaanf.chirp.api.dto.websocket

import com.kaanf.chirp.domain.type.UserId

data class ProfilePictureUpdateDto(
    val userId: UserId,
    val newUrl: String?,
)