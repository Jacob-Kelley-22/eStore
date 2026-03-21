package io.github.jacob_kelley22.eStore.dto.payment

import io.github.jacob_kelley22.eStore.entity.PaymentStatus
import java.util.UUID

data class PaymentResponseDTO(
    val paymentId: Long,
    val publicId: UUID,
    val orderId: Long,
    val status: PaymentStatus,
    val transactionId: String,
    val amount: String,
    val message: String
)
