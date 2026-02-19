package com.kaanf.chirp.api.controller

import com.kaanf.chirp.api.util.requestUserId
import com.kaanf.chirp.domain.type.ChatMessageId
import com.kaanf.chirp.service.ChatMessageService
import jdk.internal.joptsimple.internal.Messages.message
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/messages")
class ChatMessageController(
    private val chatMessageService: ChatMessageService
) {
    @DeleteMapping("/{messageId}")
    fun deleteMessage(@PathVariable messageId: ChatMessageId) {
        chatMessageService.deleteMessage(messageId = messageId, requestUserId = requestUserId)
    }
}