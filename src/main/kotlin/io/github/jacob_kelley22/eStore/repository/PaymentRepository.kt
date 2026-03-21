package io.github.jacob_kelley22.eStore.repository

import io.github.jacob_kelley22.eStore.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface PaymentRepository: JpaRepository<Payment, Long> {
    fun findByPublicId(publicId: UUID): Optional<Payment>
    fun findByTransactionId(transactionId: String): Optional<Payment>
    fun findByOrderId(orderId: Long): Optional<Payment>
}