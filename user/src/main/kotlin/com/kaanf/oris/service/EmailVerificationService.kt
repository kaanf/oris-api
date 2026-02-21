package com.kaanf.oris.service

import com.kaanf.oris.domain.event.user.UserEvent
import com.kaanf.oris.domain.exception.InvalidTokenException
import com.kaanf.oris.domain.exception.UserNotFoundException
import com.kaanf.oris.domain.model.EmailVerificationToken
import com.kaanf.oris.infra.db.entity.EmailVerificationTokenEntity
import com.kaanf.oris.infra.db.mapper.toEmailVerificationToken
import com.kaanf.oris.infra.db.mapper.toUser
import com.kaanf.oris.infra.db.repository.EmailVerificationTokenRepository
import com.kaanf.oris.infra.db.repository.UserRepository
import com.kaanf.oris.infra.message_queue.EventPublisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    @param:Value("\${oris.email.verification.expiry-hours}") private val expiryHours: Long,
    private val eventPublisher: EventPublisher
) {
    @Transactional
    fun resendVerificationEmail(email: String) {
        val token = createVerificationToken(email)

        if (token.user.hasVerifiedEmail) {
            return
        }

        eventPublisher.publish(
            event = UserEvent.RequestResendVerification(
                userId = token.user.id,
                email = token.user.email,
                username = token.user.username,
                verificationToken = token.token
            )
        )
    }

    @Transactional
    fun createVerificationToken(email: String): EmailVerificationToken {
        val userEntity = userRepository.findByEmail(email)
            ?: throw UserNotFoundException()

        emailVerificationTokenRepository.invalidateActiveTokensForUser(userEntity)

        val token = EmailVerificationTokenEntity(
            expiresAt = Instant.now().plus(expiryHours, ChronoUnit.HOURS),
            user = userEntity,
        )

        return emailVerificationTokenRepository.save(token).toEmailVerificationToken()
    }

    @Transactional
    fun verifyEmail(token: String) {
        val verificationToken = emailVerificationTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Email verification token is invalid.")

        if (verificationToken.isUsed) {
            throw InvalidTokenException("Email verification token is already used.")
        }

        if (verificationToken.isExpired) {
            throw InvalidTokenException("Email verification token has already expired.")
        }

        emailVerificationTokenRepository.save(
            verificationToken.apply {
                this.usedAt = Instant.now()
            }
        )

        userRepository.save(
            verificationToken.user.apply {
                this.hasVerifiedEmail = true
            }
        ).toUser()

        eventPublisher.publish(
            event = UserEvent.Verified(
                userId = verificationToken.user.id!!,
                email = verificationToken.user.email,
                username = verificationToken.user.username,
            )
        )
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanExpiredTokens() {
        emailVerificationTokenRepository.deleteByExpiresAtLessThan(
            currentTime = Instant.now()
        )
    }
}