package com.kaanf.chirp.infra.cache

import com.kaanf.chirp.domain.exception.RateLimitException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component

@Component
class EmailRateLimiter(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val EMAIL_RATE_LIMIT_PREFIX = "rate_limit:email"
        private const val EMAIL_ATTEMPT_COUNT_PREFIX = "email_attempt_count"
    }

    @Value("classpath:email_rate_limit.lua")
    lateinit var rateLimitResource: Resource

    private val rateLimitScript by lazy {
        val script = rateLimitResource.inputStream.use {
            it.readBytes().decodeToString()
        }
        @Suppress("UNCHECKED_CAST")
        DefaultRedisScript(script, List::class.java as Class<List<Long>>)
    }

    fun withRateLimit(email: String, action: () -> Unit) {
        val normalizedMail = email.lowercase().trim()

        val rateLimitKey = "$EMAIL_RATE_LIMIT_PREFIX:$normalizedMail"
        val attemptCountKey = "$EMAIL_ATTEMPT_COUNT_PREFIX:$normalizedMail"

        val result = redisTemplate.execute(rateLimitScript, listOf(rateLimitKey, attemptCountKey))

        val attemptCount = result[0]
        val ttl = result[1]

        if (attemptCount == -1L) {
            throw RateLimitException(resetsInSeconds = ttl)
        }

        action()
    }
}