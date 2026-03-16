package io.github.jacob_kelley22.eStore.repository

import io.github.jacob_kelley22.eStore.entity.Cart
import io.github.jacob_kelley22.eStore.entity.CartItem
import io.github.jacob_kelley22.eStore.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CartItemRepository : JpaRepository<CartItem, Long> {
    fun findByCartAndProduct(cart: Cart, product: Product): Optional<CartItem>
}