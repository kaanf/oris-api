package com.kaanf.chirp.domain.event

import com.kaanf.chirp.domain.type.UserId

data class ProfilePictureUpdatedEvent(
    val userId: UserId,
    val newUrl: String?,
)