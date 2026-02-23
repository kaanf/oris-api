package com.kaanf.oris.domain.event

import com.kaanf.oris.domain.type.UserId

data class ProfilePictureUpdatedEvent(
    val userId: UserId,
    val newUrl: String?,
)