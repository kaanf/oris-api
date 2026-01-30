package com.kaanf.chirp.service.auth

import com.kaanf.chirp.api.dto.UserDto
import com.kaanf.chirp.domain.exception.UserAlreadyExistsException
import com.kaanf.chirp.domain.model.User
import com.kaanf.chirp.infra.db.entity.UserEntity
import com.kaanf.chirp.infra.db.mapper.toUser
import com.kaanf.chirp.infra.db.repository.UserRepository
import com.kaanf.chirp.infra.security.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun register(email: String, username: String, password: String): User {
        val userExists = userRepository.existsByEmailOrUsername(
            email = email.trim(),
            username = username.trim(),
        )

        if (userExists) {
            throw UserAlreadyExistsException()
        }

        val savedUser = userRepository.save(
            UserEntity(
                email = email.trim(),
                username = username.trim(),
                hashedPassword = passwordEncoder.encode(password) ?: ""
            )
        ).toUser()

        return savedUser
    }
}
