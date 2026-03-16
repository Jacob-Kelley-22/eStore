package io.github.jacob_kelley22.eStore.dto.product

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class CreateProductRequestDTO(

    @field:NotBlank
    val name: String,

    val description: String,

    @field:Min(0)
    val price: BigDecimal,

    @field:Min(0)
    val stockQuantity: Int
)