package com.kaanf.chirp.api.controller

import com.kaanf.chirp.api.dto.AuthenticatedUserDto
import com.kaanf.chirp.api.dto.LoginRequest
import com.kaanf.chirp.api.dto.RefreshRequest
import com.kaanf.chirp.api.dto.RegisterRequest
import com.kaanf.chirp.api.dto.UserDto
import com.kaanf.chirp.api.mapper.toAuthenticatedUserDto
import com.kaanf.chirp.api.mapper.toUserDto
import com.kaanf.chirp.service.auth.AuthService
import com.kaanf.chirp.service.auth.EmailVerificationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): UserDto {
        return authService.register(
            email = request.email,
            username = request.username,
            password = request.password,
        ).toUserDto()
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): AuthenticatedUserDto {
        return authService.login(
            email = request.email,
            password = request.password
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest): AuthenticatedUserDto {
        return authService
            .refresh(request.token)
            .toAuthenticatedUserDto()
    }

    @PostMapping("/logout")
    fun logout(@RequestBody request: RefreshRequest) {
        authService.logout(request.token)
    }

    @GetMapping("/verify")
    fun verifyEmail(@RequestParam token: String) {
        emailVerificationService.verifyEmail(token)
    }
}