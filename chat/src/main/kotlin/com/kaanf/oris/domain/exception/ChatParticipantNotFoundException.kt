package com.kaanf.oris.domain.exception

import com.kaanf.oris.domain.type.UserId

class ChatParticipantNotFoundException(
    private val chatParticipantId: UserId
) : RuntimeException(
    "The chatParticipant with id $chatParticipantId was not found."
)