package com.kaanf.chirp.infra.message_queue

import com.kaanf.chirp.domain.event.ChirpEvent
import com.kaanf.chirp.domain.event.user.UserEventConstants
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.DefaultTyping
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import tools.jackson.module.kotlin.kotlinModule

@Configuration
class RabbitMQConfig {
    @Bean
    fun messageConverter(): JacksonJsonMessageConverter {
        val polymorphicTypes = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(ChirpEvent::class.java)
            .allowIfSubType("java.util.")
            .allowIfSubType("kotlin.collections.")
            .build()

        val objectMapper = JsonMapper.builder()
            .addModule(kotlinModule())
            .polymorphicTypeValidator(polymorphicTypes)
            .activateDefaultTyping(polymorphicTypes, DefaultTyping.NON_FINAL)
            .build()

        return JacksonJsonMessageConverter(objectMapper).apply {
            typePrecedence = JacksonJavaTypeMapper.TypePrecedence.TYPE_ID
        }
    }

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: JacksonJsonMessageConverter,
    ): RabbitTemplate {
        return RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }
    }

    @Bean
    fun userExchange() = TopicExchange(
        UserEventConstants.USER_EXCHANGE, true, false
    )

    @Bean
    fun notificationUserEventsQueue() = Queue(
        MessageQueues.NOTIFICATION_USER_EVENTS, true
    )

    @Bean
    fun notificationUserEventsBinding(
        notificationUserEventsQueue: Queue,
        userExchange: TopicExchange
    ): Binding {
        return BindingBuilder
            .bind(notificationUserEventsQueue)
            .to(userExchange)
            .with("user.*")
    }
}