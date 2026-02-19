package com.kaanf.chirp.api.controller

import com.kaanf.chirp.api.dto.AddParticipantToChatDto
import com.kaanf.chirp.api.dto.ChatDto
import com.kaanf.chirp.api.dto.CreateChatRequest
import com.kaanf.chirp.api.mapper.toChatDto
import com.kaanf.chirp.api.util.requestUserId
import com.kaanf.chirp.domain.type.ChatId
import com.kaanf.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(private val chatService: ChatService) {
    @PostMapping
    fun createChat(
        @Valid @RequestBody request: CreateChatRequest
    ): ChatDto {
        return chatService.createChat(
            creatorId = requestUserId,
            otherUserIds = request.otherIds.toSet()
        ).toChatDto()
    }

    @PostMapping("/{chatId}/add")
    fun addChatParticipant(
        @PathVariable("chatId") chatId: ChatId,
        @Valid @RequestBody request: AddParticipantToChatDto
    ): ChatDto {
        return chatService.addParticipants(
            requestUserId = requestUserId,
            chatId = chatId,
            userIds = request.userIds.toSet()
        ).toChatDto()
    }

    @DeleteMapping("/{chatId}/leave")
    fun leaveChat(@PathVariable("chatId") chatId: ChatId) {
        chatService.removeParticipant(
            chatId = chatId,
            userId = requestUserId
        )
    }
}