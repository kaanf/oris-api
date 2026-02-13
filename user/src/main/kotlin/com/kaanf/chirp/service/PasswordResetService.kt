package com.kaanf.chirp.service

import com.kaanf.chirp.domain.event.user.UserEvent
import com.kaanf.chirp.domain.exception.InvalidCredentialsException
import com.kaanf.chirp.domain.exception.InvalidTokenException
import com.kaanf.chirp.domain.exception.SamePasswordException
import com.kaanf.chirp.domain.exception.UserNotFoundException
import com.kaanf.chirp.domain.type.UserId
import com.kaanf.chirp.infra.db.entity.PasswordResetTokenEntity
import com.kaanf.chirp.infra.db.repository.PasswordResetTokenRepository
import com.kaanf.chirp.infra.db.repository.RefreshTokenRepository
import com.kaanf.chirp.infra.db.repository.UserRepository
import com.kaanf.chirp.infra.message_queue.EventPublisher
import com.kaanf.chirp.infra.security.PasswordEncoder
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${chirp.email.reset-password.expiry-minutes}")
    private val expiryMinutes: Long,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val eventPublisher: EventPublisher
) {
    @Transactional
    fun requestPasswordReset(email: String) {
        val user = userRepository.findByEmail(email) ?: return

        passwordResetTokenRepository.invalidateActiveTokensForUser(user)

        val token = PasswordResetTokenEntity(
            user = user,
            expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES),
        )
        passwordResetTokenRepository.save(token)

        eventPublisher.publish(
            event = UserEvent.RequestResetPassword(
                userId = user.id!!,
                email = user.email,
                username = user.username,
                expiresInMinutes = expiryMinutes,
                verificationToken = token.token
            )
        )
    }

    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Invalid password reset token.")

        if (resetToken.isUsed) {
            throw InvalidTokenException("Password reset token is already used.")
        }

        if (resetToken.isExpired) {
            throw InvalidTokenException("Password reset token has already expired.")
        }

        val user = resetToken.user

        if (passwordEncoder.matches(newPassword, user.hashedPassword)) {
            throw SamePasswordException()
        }

        val hashedNewPassword = passwordEncoder.encode(newPassword)
        userRepository.save(
            user.apply {
                this.hashedPassword = hashedNewPassword ?: return
            }
        )

        passwordResetTokenRepository.save(
            resetToken.apply {
                this.usedAt = Instant.now()
            }
        )

        refreshTokenRepository.deleteByUserId(user.id!!)
    }

    @Transactional
    fun changePassword(userId: UserId, oldPassword: String, newPassword: String) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException()

        if (!passwordEncoder.matches(oldPassword, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }

        if (oldPassword == newPassword) {
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserId(user.id!!)

        val newHashedPassword = passwordEncoder.encode(newPassword)
        userRepository.save(
            user.apply {
                this.hashedPassword = newHashedPassword ?: return
            }
        )
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiresAtLessThan(
            Instant.now()
        )
    }
}