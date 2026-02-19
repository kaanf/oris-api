package com.kaanf.chirp.infra.db.repository

import com.kaanf.chirp.domain.type.ChatId
import com.kaanf.chirp.domain.type.UserId
import com.kaanf.chirp.infra.db.entity.ChatEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatRepository : JpaRepository<ChatEntity, ChatId> {
    @Query(
        """
        SELECT c
        FROM ChatEntity c
        LEFT JOIN FETCH c.participants
        LEFT JOIN FETCH c.creator
        WHERE c.id = :id
        AND EXISTS (SELECT 1 FROM c.participants p WHERE p.userId = :userId)
    """
    )
    fun findChatById(id: ChatId, userId: UserId): ChatEntity?

    @Query(
        """
        SELECT c
        FROM ChatEntity c
        LEFT JOIN FETCH c.participants
        LEFT JOIN FETCH c.creator
        WHERE EXISTS (SELECT 1 FROM c.participants p WHERE p.userId = :userId)
    """
    )
    fun findAllByUserId(userId: UserId): List<ChatEntity>
}