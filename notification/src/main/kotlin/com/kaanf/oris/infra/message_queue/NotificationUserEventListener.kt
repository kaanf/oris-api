package com.kaanf.oris.infra.message_queue

import com.kaanf.oris.domain.event.user.UserEvent
import com.kaanf.oris.service.EmailService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Component
class NotificationUserEventListener(private val emailService: EmailService) {
    @RabbitListener(queues = [MessageQueues.NOTIFICATION_USER_EVENTS])
    @Transactional
    fun handleUserEvent(event: UserEvent) {
        when (event) {
            is UserEvent.Created -> {
                emailService.sendVerificationEmail(
                    email = event.email,
                    username = event.username,
                    token = event.verificationToken,
                    userId = event.userId
                )
            }
            is UserEvent.RequestResendVerification -> {
                emailService.sendVerificationEmail(
                    email = event.email,
                    username = event.username,
                    token = event.verificationToken,
                    userId = event.userId
                )
            }
            is UserEvent.RequestResetPassword -> {
                emailService.sendPasswordResetEmail(
                    email = event.email,
                    username = event.username,
                    token = event.verificationToken,
                    userId = event.userId,
                    expiresIn = Duration.ofMinutes(event.expiresInMinutes)
                )
            }
            else -> Unit
        }
    }
}