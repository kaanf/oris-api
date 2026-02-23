package com.kaanf.oris.api.mapper

import com.kaanf.oris.api.dto.PictureUploadResponse
import com.kaanf.oris.domain.model.ProfilePictureUploadCredentials

fun ProfilePictureUploadCredentials.toResponse(): PictureUploadResponse {
    return PictureUploadResponse(
        uploadUrl = this.uploadUrl,
        publicUrl = this.publicUrl,
        headers = this.headers,
        expiresAt = this.expiresAt
    )
}