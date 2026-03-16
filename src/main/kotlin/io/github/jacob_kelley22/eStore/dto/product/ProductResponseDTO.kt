package io.github.jacob_kelley22.eStore.dto.product

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.time.LocalDateTime

data class ProductResponseDTO(
    val id: Long,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val quantity: Int,
    val createdAt: LocalDateTime?,
    val imageUrl: String?
)