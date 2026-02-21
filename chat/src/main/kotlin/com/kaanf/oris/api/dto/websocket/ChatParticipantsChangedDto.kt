package com.kaanf.oris.api.dto.websocket

import com.kaanf.oris.domain.type.ChatId

data class ChatParticipantsChangedDto(
    val chatId: ChatId,
)