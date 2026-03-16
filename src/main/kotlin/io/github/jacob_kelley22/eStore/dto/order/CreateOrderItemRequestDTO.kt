package io.github.jacob_kelley22.eStore.dto.order

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CreateOrderItemRequestDTO(

    @field:NotNull
    val productId: Long,

    @field:Min(1)
    val quantity: Int
)
