package com.kaanf.oris.api.controller

import com.kaanf.oris.api.config.IPRateLimit
import com.kaanf.oris.api.dto.AuthenticatedUserDto
import com.kaanf.oris.api.dto.ChangePasswordRequest
import com.kaanf.oris.api.dto.EmailRequest
import com.kaanf.oris.api.dto.LoginRequest
import com.kaanf.oris.api.dto.RefreshRequest
import com.kaanf.oris.api.dto.RegisterRequest
import com.kaanf.oris.api.dto.ResetPasswordRequest
import com.kaanf.oris.api.dto.UserDto
import com.kaanf.oris.api.dto.UsernameRequest
import com.kaanf.oris.api.mapper.toAuthenticatedUserDto
import com.kaanf.oris.api.mapper.toUserDto
import com.kaanf.oris.api.util.requestUserId
import com.kaanf.oris.infra.cache.EmailRateLimiter
import com.kaanf.oris.service.AuthService
import com.kaanf.oris.service.EmailVerificationService
import com.kaanf.oris.service.PasswordResetService
import jakarta.validation.Valid
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

    @PostMapping("/username-exists")
    fun usernameExists(@Valid @RequestBody request: UsernameRequest): Boolean {
        return authService.existsByUsername(request.username)
    }

    @PostMapping("/email-exists")
    fun emailExists(@Valid @RequestBody request: EmailRequest): Boolean {
        return authService.existsByEmail(request.email)
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
        emailRateLimiter.withRateLimit(email = request.email) {
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
