package com.kaanf.chirp.infra.db.entity

import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(
    name = "refresh_tokens",
    schema = "user_service"
)
class RefreshTokenEntity(
    var id: Long = 0
) {
}