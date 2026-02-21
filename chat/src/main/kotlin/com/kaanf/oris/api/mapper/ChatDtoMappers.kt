package com.kaanf.oris.api.mapper

import com.kaanf.oris.api.dto.ChatDto
import com.kaanf.oris.api.dto.ChatMessageDto
import com.kaanf.oris.api.dto.ChatParticipantDto
import com.kaanf.oris.domain.model.Chat
import com.kaanf.oris.domain.model.ChatMessage
import com.kaanf.oris.domain.model.ChatParticipant

fun Chat.toChatDto(): ChatDto {
    return ChatDto(
        id = id,
        participants = participants.map {
            it.toChatParticipantDto()
        },
        lastActivityAt = lastActivityAt,
        lastMessage = lastMessage?.toChatMessageDto(),
        creator = creator.toChatParticipantDto()
    )
}

fun ChatMessage.toChatMessageDto(): ChatMessageDto {
    return ChatMessageDto(
        id = id,
        chatId = chatId,
        content = content,
        createdAt = createdAt,
        senderId = sender.userId
    )
}

fun ChatParticipant.toChatParticipantDto(): ChatParticipantDto {
    return ChatParticipantDto(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}