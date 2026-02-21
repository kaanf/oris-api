package com.kaanf.oris.api.mapper

import com.kaanf.oris.api.dto.AuthenticatedUserDto
import com.kaanf.oris.api.dto.UserDto
import com.kaanf.oris.domain.model.AuthenticatedUser
import com.kaanf.oris.domain.model.User

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