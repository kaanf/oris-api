package com.kaanf.oris.infra.message_queue

import com.kaanf.oris.domain.event.chat.ChatEvent
import com.kaanf.oris.service.PushNotificationService
import org.springframework.amqp.rabbit.annotation.RabbitListener

class NotificationChatEventListener(private val pushNotificationService: PushNotificationService) {
    @RabbitListener(queues = [MessageQueues.NOTIFICATION_CHAT_EVENTS])
    fun handleUserEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.NewMessage -> {
                pushNotificationService.sendNewMessageNotification(
                    recipientUserIds = event.recipientIds.toList(),
                    senderUserId = event.senderId,
                    senderUsername = event.senderUsername,
                    message = event.message,
                    chatId = event.chatId,
                )
            }
        }
    }
}