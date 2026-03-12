package com.kaanf.oris.api.dto

import org.hibernate.validator.constraints.Length

data class UsernameRequest(
    @field:Length(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
    val username: String,
)
