package com.kaanf.chirp.api.controller

import com.kaanf.chirp.api.dto.RegisterRequest
import com.kaanf.chirp.api.dto.UserDto
import com.kaanf.chirp.api.mapper.toUserDto
import com.kaanf.chirp.service.auth.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): UserDto {
        return authService.register(
            email = request.email,
            username = request.username,
            password = request.password,
        ).toUserDto()
    }
}