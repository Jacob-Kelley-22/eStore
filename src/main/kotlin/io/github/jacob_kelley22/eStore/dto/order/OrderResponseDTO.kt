package io.github.jacob_kelley22.eStore.dto.order

import io.github.jacob_kelley22.eStore.entity.OrderStatus
import java.time.LocalDateTime
import java.math.BigDecimal

data class OrderResponseDTO (
    val id: Long,
    val userId: Long,
    val items: List<OrderItemDTO>,
    val totalPrice: BigDecimal,
    val status: OrderStatus,
    val createdAt: LocalDateTime?
)