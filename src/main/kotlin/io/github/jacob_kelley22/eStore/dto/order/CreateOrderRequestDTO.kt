package io.github.jacob_kelley22.eStore.dto.order

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class CreateOrderRequestDTO (

    @field:NotNull(message = "User ID is required")
    val userId: Long,

    @field:NotEmpty(message = "Order must contain at least one product")
    val items: List<CreateOrderItemRequestDTO>
)