package com.kaanf.oris.service

import com.kaanf.oris.domain.type.ChatId
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Component

@Component
class MessageCacheEvictionHelper {
    @CacheEvict(
        value = ["messages"],
        key = "#chatId"
    )
    fun evictMessagesCache(chatId: ChatId) {
        // NO-OP, let Spring handle the cache evict.
    }
}