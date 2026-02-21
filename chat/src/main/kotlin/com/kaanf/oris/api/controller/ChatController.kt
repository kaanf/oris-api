package com.kaanf.oris.api.controller

import com.kaanf.oris.api.dto.AddParticipantToChatDto
import com.kaanf.oris.api.dto.ChatDto
import com.kaanf.oris.api.dto.ChatMessageDto
import com.kaanf.oris.api.dto.CreateChatRequest
import com.kaanf.oris.api.mapper.toChatDto
import com.kaanf.oris.api.util.requestUserId
import com.kaanf.oris.domain.type.ChatId
import com.kaanf.oris.domain.type.ChatMessageId
import com.kaanf.oris.service.ChatMessageService
import com.kaanf.oris.service.ChatService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService,
) {
    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
    }

    @GetMapping("/{chatId}/messages")
    fun getMessagesForChat(
        @PathVariable("chatId") chatId: ChatId,
        @RequestParam("before", required = false) before: Instant? = null,
        @RequestParam("pageSize", required = false) pageSize: Int = DEFAULT_PAGE_SIZE,
    ): List<ChatMessageDto> {
        return chatService.getChatMessages(
            chatId = chatId, before = before, pageSize = pageSize
        )
    }

    @GetMapping("/{chatId}")
    fun getChat(
        @PathVariable("chatId") chatId: ChatId,
    ): ChatDto {
        return chatService.getChatById(
            chatId = chatId,
            requestUserId = requestUserId
        )?.toChatDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @GetMapping
    fun getChatsForUser(): List<ChatDto> {
        return chatService.findChatsByUser(
            userId = requestUserId,
        ).map { it.toChatDto() }
    }

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