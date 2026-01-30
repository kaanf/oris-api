package com.kaanf.chirp.infra.db.repository

import com.kaanf.chirp.infra.db.entity.EmailVerificationTokenEntity
import com.kaanf.chirp.infra.db.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface EmailVerificationTokenRepository: JpaRepository<EmailVerificationTokenEntity, Long> {
    fun findByToken(token: String): EmailVerificationTokenEntity?
    fun deleteByExpiresAtLessThan(currentTime: Instant)
    fun findByUserAndUsedAtIsNull(user: UserEntity): List<EmailVerificationTokenEntity>
}