package com.kaanf.chirp.api.config

import com.kaanf.chirp.domain.exception.RateLimitException
import com.kaanf.chirp.infra.cache.IPRateLimiter
import com.kaanf.chirp.infra.cache.IPResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration

@Component
class IPRateLimitInterceptor(
    private val ipRateLimiter: IPRateLimiter,
    private val ipResolver: IPResolver,
    @param:Value("\${chirp.rate-limit.ip.apply-limit}") private val applyLimit: Boolean,
): HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod && applyLimit) {
            val annotation = handler.getMethodAnnotation(IPRateLimit::class.java)

            if (annotation != null) {
                val clientIp = ipResolver.getClientIp(request)

                return try {
                    ipRateLimiter.withIpRateLimit(
                        ipAddress = clientIp,
                        resetIn = Duration.of(annotation.duration, annotation.unit.toChronoUnit()),
                        maxRequestsPerIp = annotation.requests,
                        action = { true }
                    )
                } catch (e: RateLimitException) {
                    response.sendError(429)
                    false
                }
            }
        }

        return true
    }
}