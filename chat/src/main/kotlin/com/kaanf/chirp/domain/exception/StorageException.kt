package com.kaanf.chirp.domain.exception

class StorageException(override val message: String?): RuntimeException(message ?: "Unable to store file.")