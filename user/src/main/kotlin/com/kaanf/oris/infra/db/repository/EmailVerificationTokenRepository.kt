package com.kaanf.oris.infra.db.repository

import com.kaanf.oris.infra.db.entity.EmailVerificationTokenEntity
import com.kaanf.oris.infra.db.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface EmailVerificationTokenRepository: JpaRepository<EmailVerificationTokenEntity, Long> {
    fun findByToken(token: String): EmailVerificationTokenEntity?
    fun deleteByExpiresAtLessThan(currentTime: Instant)

    @Modifying
    @Query("""
        UPDATE EmailVerificationTokenEntity e SET e.usedAt = CURRENT_TIMESTAMP WHERE e.user = :user
    """)
    fun invalidateActiveTokensForUser(user: UserEntity)
}