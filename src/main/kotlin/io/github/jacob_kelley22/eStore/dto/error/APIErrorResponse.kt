package io.github.jacob_kelley22.eStore.dto.error

import java.time.LocalDateTime

data class APIErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val fieldErrors: List<FieldErrorResponse>? = null
)
