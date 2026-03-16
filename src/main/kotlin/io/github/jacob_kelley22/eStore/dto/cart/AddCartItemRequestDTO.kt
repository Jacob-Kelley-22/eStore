package io.github.jacob_kelley22.eStore.dto.cart

import jakarta.validation.constraints.Min

data class AddCartItemRequestDTO(
    val productId: Long,

    @field:Min(1)
    val quantity: Int
)
