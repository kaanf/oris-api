package com.kaanf.chirp.domain.exception

class UserAlreadyExistsException: RuntimeException("A user with that name or mail already exists.")