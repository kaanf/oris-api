package com.kaanf.chirp.api.mapper

import com.kaanf.chirp.api.dto.PictureUploadResponse
import com.kaanf.chirp.domain.model.ProfilePictureUploadCredentials

fun ProfilePictureUploadCredentials.toResponse(): PictureUploadResponse {
    return PictureUploadResponse(
        uploadUrl = this.uploadUrl,
        publicUrl = this.publicUrl,
        headers = this.headers,
        expiresAt = this.expiresAt
    )
}