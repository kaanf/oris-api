package com.kaanf.chirp.domain.exception

import com.kaanf.chirp.domain.type.ChatMessageId

class MessageNotFoundException(
    private val id: ChatMessageId
): RuntimeException("Message with ID $id not found.")