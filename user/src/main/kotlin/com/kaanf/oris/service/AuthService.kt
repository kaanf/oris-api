package com.kaanf.oris.service

import com.kaanf.oris.domain.event.user.UserEvent
import com.kaanf.oris.domain.exception.EmailNotVerifiedException
import com.kaanf.oris.domain.exception.InvalidCredentialsException
import com.kaanf.oris.domain.exception.InvalidTokenException
import com.kaanf.oris.domain.exception.UserAlreadyExistsException
import com.kaanf.oris.domain.exception.UserNotFoundException
import com.kaanf.oris.domain.model.AuthenticatedUser
import com.kaanf.oris.domain.model.User
import com.kaanf.oris.domain.type.UserId
import com.kaanf.oris.infra.db.entity.RefreshTokenEntity
import com.kaanf.oris.infra.db.entity.UserEntity
import com.kaanf.oris.infra.db.mapper.toUser
import com.kaanf.oris.infra.db.repository.RefreshTokenRepository
import com.kaanf.oris.infra.db.repository.UserRepository
import com.kaanf.oris.infra.message_queue.EventPublisher
import com.kaanf.oris.infra.security.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationService: EmailVerificationService,
    private val eventPublisher: EventPublisher
) {
    @Transactional
    fun register(email: String, username: String, password: String): User {
        val trimmedEmail = email.trim()

        val isUserExists = userRepository.existsByEmailOrUsername(
            email = trimmedEmail,
            username = username.trim(),
        )

        if (isUserExists) {
            throw UserAlreadyExistsException()
        }

        val savedUser = userRepository.saveAndFlush(
            UserEntity(
                email = trimmedEmail,
                username = username.trim(),
                hashedPassword = passwordEncoder.encode(password) ?: ""
            )
        ).toUser()

        val token = emailVerificationService.createVerificationToken(trimmedEmail)

        eventPublisher.publish(
            event = UserEvent.Created(
                userId = savedUser.id,
                email = savedUser.email,
                username = savedUser.username,
                verificationToken = token.token
            )
        )

        return savedUser
    }

    fun login(email: String, password: String): AuthenticatedUser {
        val user = userRepository.findByEmail(email.trim())
            ?: throw InvalidCredentialsException()

        if (!passwordEncoder.matches(password, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }

        if (!user.hasVerifiedEmail) {
            throw EmailNotVerifiedException()
        }

        return user.id?.let { userId ->
            val accessToken = jwtService.generateAccessToken(userId)
            val refreshToken = jwtService.generateRefreshToken(userId)

            storeRefreshToken(userId, refreshToken)

            AuthenticatedUser(
                user = user.toUser(),
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } ?: throw UserNotFoundException()
    }

    fun existsByUsername(username: String): Boolean {
        return userRepository.existsByUsername(username.trim())
    }

    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email.trim())
    }

    @Transactional
    fun refresh(refreshToken: String): AuthenticatedUser {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw InvalidTokenException(message = "Invalid refresh token.",)
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()

        val hashed = hashToken(refreshToken)

        return user.id?.let { userId ->
            refreshTokenRepository.findByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashed
            ) ?: throw InvalidTokenException("Invalid refresh token.")

            refreshTokenRepository.deleteByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashed
            )

            val newAccessToken = jwtService.generateAccessToken(userId)
            val newRefreshToken = jwtService.generateRefreshToken(userId)

            storeRefreshToken(userId, newRefreshToken)

            AuthenticatedUser(
                user = user.toUser(),
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            )
        } ?: throw UserNotFoundException()
    }

    @Transactional
    fun logout(refreshToken: String) {
        val userId = jwtService.getUserIdFromToken(refreshToken)
        val hashed = hashToken(refreshToken)

        refreshTokenRepository.deleteByUserIdAndHashedToken(userId, hashed)
    }

    private fun storeRefreshToken(userId: UserId, token: String) {
        val hashed = hashToken(token)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshTokenEntity(userId = userId, expiresAt = expiresAt, hashedToken = hashed)
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}
