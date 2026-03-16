package io.github.jacob_kelley22.eStore.mapper

import io.github.jacob_kelley22.eStore.dto.cart.CartItemResponseDTO
import io.github.jacob_kelley22.eStore.dto.cart.CartResponseDTO
import io.github.jacob_kelley22.eStore.dto.cart.AddCartItemRequestDTO
import io.github.jacob_kelley22.eStore.entity.CartItem
import io.github.jacob_kelley22.eStore.entity.Cart
import java.math.BigDecimal

fun CartItem.toDTO(): CartItemResponseDTO {
    val lineTotal = product.price.multiply(BigDecimal(quantity))
    return CartItemResponseDTO(
        productId = product.id,
        productName = product.name,
        quantity = quantity,
        unitPrice = product.price,
        lineTotal = lineTotal
    )
}

fun Cart.toDTO(): CartResponseDTO {
    val itemDTOs = items.map { it.toDTO() }
    val total = itemDTOs.fold(BigDecimal.ZERO) { acc, item ->
        acc + item.lineTotal
    }

    return CartResponseDTO(
        userId = user.id,
        items = itemDTOs,
        totalPrice = total
    )
}