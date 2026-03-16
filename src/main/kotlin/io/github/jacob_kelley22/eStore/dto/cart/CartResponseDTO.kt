package io.github.jacob_kelley22.eStore.dto.cart

import java.math.BigDecimal

data class CartResponseDTO(
    val userId: Long,
    val items: List<CartItemResponseDTO>,
    val totalPrice: BigDecimal
)
