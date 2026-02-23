package com.kaanf.oris.domain.exception

class InvalidTokenException(override val message: String?): RuntimeException(message ?: "Invalid token")