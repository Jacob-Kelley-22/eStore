package io.github.jacob_kelley22.eStore.dto.order

import java.math.BigDecimal

/* This is not a response since it will not be
returned to a client */
data class OrderItemDTO(
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val priceAtPurchase: BigDecimal,
    val lineTotal: BigDecimal
)
