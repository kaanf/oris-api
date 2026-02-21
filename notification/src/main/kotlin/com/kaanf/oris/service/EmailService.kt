package com.kaanf.oris.service

import com.kaanf.oris.domain.type.UserId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.time.Duration

@Service
class EmailService(
    private val javaMailSender: JavaMailSender,
    private val templateService: EmailTemplateService,
    @param:Value("\${oris.email.from}") private val emailFrom: String,
    @param:Value("\${oris.email.url}") private val baseurl: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun sendVerificationEmail(email: String, username: String, userId: UserId, token: String) {
        logger.info("Sending verification email for user $userId")

        val verificationUrl = UriComponentsBuilder
            .fromUriString("$baseurl/api/auth/verify")
            .queryParam("token", token)
            .build()
            .toUriString()

        val htmlContent = templateService.processTemplate(
            templateName = "emails/account-verification",
            variables = mapOf(
                "username" to username,
                "verificationUrl" to verificationUrl,
                "expiresIn" to 3600,
                "email" to email
            )
        )

        sendHtmlEmail(
            to = email,
            subject = "Verify your oris account for verification.",
            html = htmlContent,
        )
    }

    private fun sendHtmlEmail(to: String, subject: String, html: String) {
        val message = javaMailSender.createMimeMessage()
        MimeMessageHelper(message, true, "UTF-8").apply {
            setFrom(emailFrom)
            setTo(to)
            setSubject(subject)
            setText(html, true)
        }

        try {
            javaMailSender.send(message)
        } catch (e: MailException) {
            logger.error("Could not send mail.", e)
        }
    }

    fun sendPasswordResetEmail(
        email: String, username: String, userId: UserId, token: String, expiresIn: Duration
    ) {
        logger.info("Sending password reset email for user $userId")

        val resetPasswordUrl = UriComponentsBuilder
            .fromUriString("$baseurl/api/auth/reset-password")
            .queryParam("token", token)
            .build()
            .toUriString()

        val htmlContent = templateService.processTemplate(
            templateName = "emails/reset-password",
            variables = mapOf(
                "username" to username,
                "resetUrl" to resetPasswordUrl,
                "expiresIn" to expiresIn,
                "email" to email
            )
        )

        sendHtmlPassword(
            to = email,
            subject = "Reset your Oris password.",
            html = htmlContent,
        )
    }

    private fun sendHtmlPassword(to: String, subject: String, html: String) {
        val message = javaMailSender.createMimeMessage()
        MimeMessageHelper(message, true, "UTF-8").apply {
            setFrom(emailFrom)
            setTo(to)
            setSubject(subject)
            setText(html, true)
        }

        try {
            javaMailSender.send(message)
        } catch (e: MailException) {
            logger.error("Could not send mail.", e)
        }
    }
}