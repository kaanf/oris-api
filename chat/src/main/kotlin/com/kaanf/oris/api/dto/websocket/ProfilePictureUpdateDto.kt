package com.kaanf.oris.api.dto.websocket

import com.kaanf.oris.domain.type.UserId

data class ProfilePictureUpdateDto(
    val userId: UserId,
    val newUrl: String?,
)