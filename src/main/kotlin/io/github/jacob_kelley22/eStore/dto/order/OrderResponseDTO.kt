package io.github.jacob_kelley22.eStore.dto.order

import java.time.LocalDateTime
import java.math.BigDecimal

data class OrderResponseDTO (
    val id: Long,
    val userId: Long,
    val items: List<OrderItemDTO>,
    val totalPrice: BigDecimal,
    val createdAt: LocalDateTime?
)