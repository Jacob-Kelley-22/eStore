package io.github.jacob_kelley22.eStore.dto.cart

import java.math.BigDecimal

data class CartItemResponseDTO(
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val lineTotal: BigDecimal
)
