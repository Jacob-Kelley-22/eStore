package io.github.jacob_kelley22.eStore.dto.payment

data class PaymentResponseDTO(
    val approved: Boolean,
    val message: String,
    val transactionId: String? = null
)
