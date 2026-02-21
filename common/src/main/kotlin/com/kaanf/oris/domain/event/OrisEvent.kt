package com.kaanf.oris.domain.event

import java.time.Instant

interface OrisEvent {
    val eventId: String
    val eventKey: String
    val occurredAt: Instant
    val exchange: String
}