package com.kaanf.oris.service

import com.kaanf.oris.domain.exception.InvalidDeviceTokenException
import com.kaanf.oris.domain.model.DeviceToken
import com.kaanf.oris.domain.model.PushNotification
import com.kaanf.oris.domain.type.ChatId
import com.kaanf.oris.domain.type.UserId
import com.kaanf.oris.infra.db.DeviceTokenEntity
import com.kaanf.oris.infra.db.DeviceTokenRepository
import com.kaanf.oris.infra.mapper.toDeviceToken
import com.kaanf.oris.infra.mapper.toPlatformEntity
import com.kaanf.oris.infra.push_notification.FirebasePushNotificationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentSkipListMap

@Service
class PushNotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val firebasePushNotificationService: FirebasePushNotificationService
) {
    companion object {
        private val RETRY_DELAYS_SECONDS = listOf(
            30L, 60L, 120L, 300L, 600L
        )

        const val MAX_RETRY_AGE_MINUTES = 30L
    }

    private val retryQueue = ConcurrentSkipListMap<Long, MutableList<RetryData>>()

    private val logger = LoggerFactory.getLogger(PushNotificationService::class.java)

    @Transactional
    fun registerDevice(userId: UserId, token: String, platform: DeviceToken.Platform): DeviceToken {
        val existing = deviceTokenRepository.findByToken(token)

        val trimmedToken = token.trim()

        if (existing == null && !firebasePushNotificationService.isValidToken(trimmedToken)) {
            throw InvalidDeviceTokenException()
        }

        val entity = if (existing != null) {
            deviceTokenRepository.save(
                existing.apply {
                    this.userId = userId
                }
            )
        } else {
            deviceTokenRepository.save(
                DeviceTokenEntity(
                    userId = userId,
                    token = trimmedToken,
                    platform = platform.toPlatformEntity()
                )
            )
        }

        return entity.toDeviceToken()
    }

    @Transactional
    fun unregisterDevice(token: String) {
        deviceTokenRepository.deleteByToken(token)
    }

    fun sendNewMessageNotification(
        recipientUserIds: List<UserId>,
        senderUserId: UserId,
        senderUsername: String,
        message: String,
        chatId: ChatId,
    ) {
        val deviceTokens = deviceTokenRepository.findByUserIdIn(recipientUserIds)
        if (deviceTokens.isEmpty()) {
            logger.info("No device tokens found for $recipientUserIds")
            return
        }

        val recipients = deviceTokens
            .filter { it.userId != senderUserId }
            .map { it.toDeviceToken() }

        val notification = PushNotification(
            title = "New message from $senderUsername",
            recipients = recipients,
            message = message,
            chatId = chatId,
            data = mapOf(
                "chatId" to chatId.toString(),
                "type" to "new_message"
            )
        )

        sendWithRetry(notification)
    }

    fun sendWithRetry(
        notification: PushNotification,
        attempt: Int = 0
    ) {
        val result = firebasePushNotificationService.sendNotification(notification)

        result.permanentFailures.forEach {
            deviceTokenRepository.deleteByToken(it.token)
        }

        if (result.temporaryFailures.isNotEmpty() && attempt < RETRY_DELAYS_SECONDS.size) {
            val retryNotification = notification.copy(
                recipients = result.temporaryFailures
            )

            scheduleRetry(retryNotification, attempt + 1)
        }

        if (result.succeeded.isNotEmpty()) {
            logger.info("Successfully sent message to ${result.succeeded.size} devices.")
        }
    }

    private fun scheduleRetry(
        notification: PushNotification,
        attempt: Int
    ) {
        val delay = RETRY_DELAYS_SECONDS.getOrElse(attempt - 1) {
            RETRY_DELAYS_SECONDS.last()
        }

        val executeAt = Instant.now().plusSeconds(delay)
        val executeAtMillis = executeAt.toEpochMilli()

        val retryData = RetryData(
            notification = notification,
            attempt = attempt,
            createdAt = Instant.now(),
        )

        retryQueue.compute(executeAtMillis) { _, retries ->
            (retries ?: mutableListOf()).apply { add(retryData) }
        }

        logger.info("Scheduled retry $attempt for ${notification.id} in $delay seconds.")
    }

    @Scheduled(fixedDelay = 15_000L)
    fun processRetries() {
        val now = Instant.now()
        val nowMillis = now.toEpochMilli()
        val toProcess = retryQueue.headMap(nowMillis, true)

        if (toProcess.isEmpty()) {
            return
        }

        val entries = toProcess.entries.toList()
        entries.forEach { (timeMillis, retries) ->
            retryQueue.remove(timeMillis)

            retries.forEach { retry ->
                try {
                    val age = Duration.between(retry.createdAt, now)
                    if (age.toMinutes() > MAX_RETRY_AGE_MINUTES) {
                        logger.warn("Dropping old retry (${age.toMinutes()} old.)")
                        return@forEach
                    }

                    sendWithRetry(
                        notification = retry.notification,
                        attempt = retry.attempt,
                    )
                } catch (e: Exception) {
                    logger.warn("Error processing retry ${retry.notification.id}", e)
                }
            }
        }
    }

    private data class RetryData(
        val notification: PushNotification,
        val attempt: Int,
        val createdAt: Instant
    )
}