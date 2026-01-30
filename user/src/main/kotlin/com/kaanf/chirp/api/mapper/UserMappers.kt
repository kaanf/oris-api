package com.kaanf.chirp.api.mapper

import com.kaanf.chirp.api.dto.AuthenticatedUserDto
import com.kaanf.chirp.api.dto.UserDto
import com.kaanf.chirp.domain.model.AuthenticatedUser
import com.kaanf.chirp.domain.model.User

fun AuthenticatedUser.toAuthenticatedUserDto(): AuthenticatedUserDto {
    return AuthenticatedUserDto(
        user = user.toUserDto(),
        accessToken = accessToken,
        refreshToken = refreshToken,
    )
}

fun User.toUserDto(): UserDto {
    return UserDto(
        id = id,
        username = username,
        email = email,
        hasVerifiedEmail = hasVerifiedEmail
    )
}