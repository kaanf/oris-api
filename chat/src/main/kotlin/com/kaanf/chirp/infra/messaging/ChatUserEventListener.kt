package com.kaanf.chirp.infra.messaging

import com.kaanf.chirp.domain.event.user.UserEvent
import com.kaanf.chirp.domain.model.ChatParticipant
import com.kaanf.chirp.infra.message_queue.MessageQueues
import com.kaanf.chirp.service.ChatParticipantService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ChatUserEventListener(
    private val chatParticipantService: ChatParticipantService,
) {
    @RabbitListener(queues = [MessageQueues.CHAT_USER_EVENTS])
    fun handleUserEvent(event: UserEvent) {
        when(event) {
            is UserEvent.Verified -> {
                chatParticipantService.createChatParticipant(
                    chatParticipant = ChatParticipant(
                        userId = event.userId,
                        username = event.username,
                        email = event.email,
                        profilePictureUrl = null
                    )
                )
            }
            else -> Unit
        }
    }
}