package com.kaanf.chirp.domain.exception

import com.kaanf.chirp.domain.type.UserId

class ChatParticipantNotFoundException(
    private val chatParticipantId: UserId
) : RuntimeException(
    "The chatParticipant with id $chatParticipantId was not found."
)