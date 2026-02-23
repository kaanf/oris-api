package com.kaanf.oris.api.config

import java.util.concurrent.TimeUnit

annotation class IPRateLimit(
    val requests: Int = 60,
    val duration: Long = 1L,
    val unit: TimeUnit = TimeUnit.HOURS
)