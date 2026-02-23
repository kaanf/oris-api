package com.kaanf.oris.domain.exception

import com.kaanf.oris.domain.type.ChatMessageId

class MessageNotFoundException(
    private val id: ChatMessageId
): RuntimeException("Message with ID $id not found.")