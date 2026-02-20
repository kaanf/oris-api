package com.kaanf.chirp.api.dto.websocket

import com.kaanf.chirp.domain.type.ChatId

data class ChatParticipantsChangedDto(
    val chatId: ChatId,
)