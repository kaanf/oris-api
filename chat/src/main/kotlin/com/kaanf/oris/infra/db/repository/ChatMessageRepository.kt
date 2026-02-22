package com.kaanf.oris.infra.db.repository

import com.kaanf.oris.domain.type.ChatId
import com.kaanf.oris.domain.type.ChatMessageId
import com.kaanf.oris.infra.db.entity.ChatEntity
import com.kaanf.oris.infra.db.entity.ChatMessageEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface ChatMessageRepository : JpaRepository<ChatMessageEntity, ChatMessageId> {
    @Query(
        """
        SELECT m
        FROM ChatMessageEntity m
        WHERE m.chatId = :chatId
        AND m.createdAt < :before
        ORDER BY m.createdAt DESC
    """
    )
    fun findByChatIdBefore(chatId: ChatId, before: Instant, pageable: Pageable): Slice<ChatMessageEntity>

    @Query(
        """
        SELECT m
        FROM ChatMessageEntity m
        LEFT JOIN FETCH m.sender
        WHERE m.chatId IN :chatIds
        AND (m.createdAt, m.id) = (
            SELECT m2.createdAt, m2.id
            FROM ChatMessageEntity m2
            WHERE m2.id = m.chatId
            ORDER BY m2.createdAt DESC
            LIMIT 1
        )
    """
    )
    fun findLatestMessagesByChatIds(chatIds: Set<ChatId>): List<ChatMessageEntity>
}