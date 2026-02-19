package com.kaanf.chirp.domain.event.chat

import com.kaanf.chirp.domain.event.ChirpEvent
import com.kaanf.chirp.domain.type.ChatId
import com.kaanf.chirp.domain.type.UserId
import java.time.Instant
import java.util.UUID

sealed class ChatEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val exchange: String = ChatEventConstants.CHAT_EXCHANGE,
    override val occurredAt: Instant = Instant.now(),
): ChirpEvent {
    data class NewMessage(
        val senderId: UserId,
        val senderUsername: String,
        val recipientIds: Set<UserId>,
        val chatId: ChatId,
        val message: String,
        override val eventKey: String = ChatEventConstants.CHAT_NEW_MESSAGE,
    ): ChatEvent(), ChirpEvent
}