package io.github.jacob_kelley22.eStore.mapper

import io.github.jacob_kelley22.eStore.dto.product.ProductResponseDTO
import io.github.jacob_kelley22.eStore.entity.Product

fun Product.toDTO() : ProductResponseDTO {
    return ProductResponseDTO(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        quantity = this.stockQuantity,
        createdAt = this.createdAt,
        imageUrl = this.imageUrl
    )
}