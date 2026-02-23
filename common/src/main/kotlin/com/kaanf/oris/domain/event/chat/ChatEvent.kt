package com.kaanf.oris.domain.event.chat

import com.kaanf.oris.domain.event.OrisEvent
import com.kaanf.oris.domain.type.ChatId
import com.kaanf.oris.domain.type.UserId
import java.time.Instant
import java.util.UUID

sealed class ChatEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val exchange: String = ChatEventConstants.CHAT_EXCHANGE,
    override val occurredAt: Instant = Instant.now(),
): OrisEvent {
    data class NewMessage(
        val senderId: UserId,
        val senderUsername: String,
        val recipientIds: Set<UserId>,
        val chatId: ChatId,
        val message: String,
        override val eventKey: String = ChatEventConstants.CHAT_NEW_MESSAGE,
    ): ChatEvent(), OrisEvent
}