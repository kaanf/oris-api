package com.kaanf.oris.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern

data class LoginRequest(
    @field:Email(message = "Must be a valid email address.")
    val email: String,
    @field:Pattern(
        regexp = "^(?=.*[\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])(.{8,})$",
        message = "Password must be at least 8 characters and contain at least one digit or special character."
    )
    val password: String,
)