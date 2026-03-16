package io.github.jacob_kelley22.eStore.mapper

import io.github.jacob_kelley22.eStore.dto.order.OrderItemDTO
import io.github.jacob_kelley22.eStore.dto.order.OrderResponseDTO
import io.github.jacob_kelley22.eStore.entity.Order
import io.github.jacob_kelley22.eStore.entity.OrderItem
import java.math.BigDecimal

fun Order.toDTO() : OrderResponseDTO {
    return OrderResponseDTO(
        id = this.id,
        userId = this.user.id,
        items = this.items.map { it.toDTO() },
        totalPrice = this.totalPrice,
        createdAt = this.createdAt
    )
}

fun OrderItem.toDTO() : OrderItemDTO {
    return OrderItemDTO(
        productId = this.product.id,
        productName = this.product.name,
        quantity = this.quantity,
        priceAtPurchase = this.priceAtPurchase,
        lineTotal = this.priceAtPurchase.multiply(BigDecimal(this.quantity))
    )
}