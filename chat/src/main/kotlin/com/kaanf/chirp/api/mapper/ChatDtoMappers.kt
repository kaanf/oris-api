package com.kaanf.chirp.api.mapper

import com.kaanf.chirp.api.dto.ChatDto
import com.kaanf.chirp.api.dto.ChatMessageDto
import com.kaanf.chirp.api.dto.ChatParticipantDto
import com.kaanf.chirp.domain.model.Chat
import com.kaanf.chirp.domain.model.ChatMessage
import com.kaanf.chirp.domain.model.ChatParticipant

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