package com.kaanf.chirp.infra.storage

import com.kaanf.chirp.domain.exception.InvalidProfilePictureException
import com.kaanf.chirp.domain.exception.StorageException
import com.kaanf.chirp.domain.model.ProfilePictureUploadCredentials
import com.kaanf.chirp.domain.type.UserId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.Instant
import java.util.UUID

@Service
class SupabaseStorageService(
    @param:Value("\${supabase.url}") private val supabaseUrl: String,
    private val supabaseRestClient: RestClient,
) {
    companion object {
        private const val profilePicturesBucket = "profile-pictures"

        private val allowedMimeTypes = mapOf(
            "image/jpeg" to "jpg",
            "image/jpg" to "jpg",
            "image/png" to "png",
            "image/webp" to "webp",
        )
    }

    private val normalizedSupabaseUrl = supabaseUrl.removeSuffix("/")

    fun generateSignedUploadUrl(userId: UserId, mimeType: String): ProfilePictureUploadCredentials {
        val extension = allowedMimeTypes[mimeType]
            ?: throw InvalidProfilePictureException("Invalid mime type $mimeType")

        val fileName = "user_${userId}_${UUID.randomUUID()}.$extension"
        val objectPath = "$profilePicturesBucket/$fileName"

        val publicUrl = "$normalizedSupabaseUrl/storage/v1/object/public/$objectPath"

        return ProfilePictureUploadCredentials(
            uploadUrl = createSignedUrl(
                bucketName = profilePicturesBucket,
                pathInBucket = fileName,
                expiresInSeconds = 300
            ),
            publicUrl = publicUrl,
            headers = mapOf(
                "Content-Type" to mimeType,
            ),
            expiresAt = Instant.now().plusSeconds(300)
        )
    }

    fun deleteFile(url: String) {
        val publicUrlPrefix = "$normalizedSupabaseUrl/storage/v1/object/public/"
        val objectPath = if (url.startsWith(publicUrlPrefix)) {
            url.substringAfter(publicUrlPrefix)
        } else {
            throw StorageException("Invalid file URL format.")
        }

        if (objectPath.isBlank()) {
            throw StorageException("Invalid file URL format.")
        }

        val deleteUrl = "/storage/v1/object/$objectPath"

        val response = supabaseRestClient
            .delete()
            .uri(deleteUrl)
            .retrieve()
            .toBodilessEntity()

        if (response.statusCode.isError) {
            throw StorageException("Unable to delete file: ${response.statusCode}")
        }
    }

    private fun createSignedUrl(
        bucketName: String,
        pathInBucket: String,
        expiresInSeconds: Int
    ): String {
        val json = """
            { "expiresIn": $expiresInSeconds }
        """.trimIndent()

        val response = supabaseRestClient
            .post()
            .uri("/storage/v1/object/upload/sign/$bucketName/$pathInBucket")
            .header("Content-Type", "application/json")
            .body(json)
            .retrieve()
            .body(SignedUploadResponse::class.java)
            ?: throw StorageException("Failed to create signed URL.")

        return "$normalizedSupabaseUrl/storage/v1${response.url}"
    }

    private data class SignedUploadResponse(val url: String)
}
