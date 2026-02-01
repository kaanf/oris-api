package com.kaanf.chirp.infra.db.repository

import com.kaanf.chirp.infra.db.entity.PasswordResetTokenEntity
import com.kaanf.chirp.infra.db.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface PasswordResetTokenRepository: JpaRepository<PasswordResetTokenEntity, Long> {
    fun findByToken(token: String): PasswordResetTokenEntity?
    fun deleteByExpiresAtLessThan(currentTime: Instant)
    @Modifying
    @Query("""
        UPDATE PasswordResetTokenEntity p SET p.usedAt = CURRENT_TIMESTAMP WHERE p.user = :user
    """)
    fun invalidateActiveTokensForUser(user: UserEntity)
}