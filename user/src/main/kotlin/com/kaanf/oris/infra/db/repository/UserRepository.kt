package com.kaanf.oris.infra.db.repository

import com.kaanf.oris.infra.db.entity.UserEntity
import com.kaanf.oris.domain.type.UserId
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<UserEntity, UserId> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
    fun existsByEmailOrUsername(email: String, username: String): Boolean
}
