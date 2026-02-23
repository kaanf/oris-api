package com.kaanf.oris.api.websocket

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val handler: ChatWebSocketHandler,
    @param:Value("\${oris.web-socket.allowed-origin}") private val allowedOrigin: String
): WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(handler, "/websocket/chat")
            .setAllowedOrigins(
                allowedOrigin
            )
    }
}