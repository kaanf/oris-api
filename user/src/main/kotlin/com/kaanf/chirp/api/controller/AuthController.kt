package com.kaanf.chirp.api.controller

import com.kaanf.chirp.api.config.IPRateLimit
import com.kaanf.chirp.api.dto.AuthenticatedUserDto
import com.kaanf.chirp.api.dto.ChangePasswordRequest
import com.kaanf.chirp.api.dto.EmailRequest
import com.kaanf.chirp.api.dto.LoginRequest
import com.kaanf.chirp.api.dto.RefreshRequest
import com.kaanf.chirp.api.dto.RegisterRequest
import com.kaanf.chirp.api.dto.ResetPasswordRequest
import com.kaanf.chirp.api.dto.UserDto
import com.kaanf.chirp.api.mapper.toAuthenticatedUserDto
import com.kaanf.chirp.api.mapper.toUserDto
import com.kaanf.chirp.api.util.requestUserId
import com.kaanf.chirp.infra.cache.EmailRateLimiter
import com.kaanf.chirp.service.AuthService
import com.kaanf.chirp.service.EmailVerificationService
import com.kaanf.chirp.service.PasswordResetService
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService,
    private val emailRateLimiter: EmailRateLimiter
) {
    @PostMapping("/register")
    @IPRateLimit(requests = 10, duration = 1L, unit = TimeUnit.HOURS)
    fun register(@Valid @RequestBody request: RegisterRequest): UserDto {
        return authService.register(
            email = request.email,
            username = request.username,
            password = request.password,
        ).toUserDto()
    }

    @PostMapping("/login")
    @IPRateLimit(requests = 10, duration = 1L, unit = TimeUnit.HOURS)
    fun login(@RequestBody request: LoginRequest): AuthenticatedUserDto {
        return authService.login(
            email = request.email,
            password = request.password
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    @IPRateLimit(requests = 10, duration = 1L, unit = TimeUnit.HOURS)
    fun refresh(@RequestBody request: RefreshRequest): AuthenticatedUserDto {
        return authService
            .refresh(request.token)
            .toAuthenticatedUserDto()
    }

    @PostMapping("/logout")
    fun logout(@RequestBody request: RefreshRequest) {
        authService.logout(request.token)
    }

    @PostMapping("/resend-verification")
    @IPRateLimit(requests = 10, duration = 1L, unit = TimeUnit.HOURS)
    fun resendVerification(@Valid @RequestBody request: EmailRequest) {
        emailRateLimiter.withRateLimit(
            email = request.email
        ) {
            emailVerificationService.resendVerificationEmail(request.email)
        }
    }

    @GetMapping("/verify")
    fun verifyEmail(@RequestParam token: String) {
        emailVerificationService.verifyEmail(token)
    }

    @PostMapping("/forgot-password")
    @IPRateLimit(requests = 10, duration = 1L, unit = TimeUnit.HOURS)
    fun forgotPassword(@Valid @RequestBody request: EmailRequest) {
        passwordResetService.requestPasswordReset(request.email)
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest) {
        passwordResetService.resetPassword(
            token = request.token,
            newPassword = request.newPassword
        )
    }

    @PostMapping("/change-password")
    fun changePassword(@Valid @RequestBody request: ChangePasswordRequest) {
        passwordResetService.changePassword(
            userId = requestUserId,
            oldPassword = request.oldPassword,
            newPassword = request.newPassword
        )
    }
}