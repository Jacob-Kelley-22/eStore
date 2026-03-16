package io.github.jacob_kelley22.eStore.controller

import io.github.jacob_kelley22.eStore.dto.cart.AddCartItemRequestDTO
import io.github.jacob_kelley22.eStore.dto.cart.CartResponseDTO
import io.github.jacob_kelley22.eStore.dto.order.OrderResponseDTO
import io.github.jacob_kelley22.eStore.dto.payment.PaymentRequestDTO
import io.github.jacob_kelley22.eStore.service.AuthenticationFacade
import io.github.jacob_kelley22.eStore.service.CartService
import io.github.jacob_kelley22.eStore.service.OrderService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@Tag(name = "Cart", description = "Cart management endpoints")
@RestController
@RequestMapping("/api/cart")
class CartController(
    private val cartService: CartService,
    private val orderService: OrderService,
    private val authenticationFacade: AuthenticationFacade
) {

    @GetMapping
    fun getCart(): ResponseEntity<CartResponseDTO> {
        val email = authenticationFacade.getCurrentUserEmail()
        return ResponseEntity.ok(cartService.getCartByUserEmail(email))
    }

    @PostMapping("/items")
    fun addItemToCart(
        @RequestBody @Valid request: AddCartItemRequestDTO
    ): ResponseEntity<CartResponseDTO> {
        val email = authenticationFacade.getCurrentUserEmail()
        return ResponseEntity.ok(cartService.addItemToCartByUserEmail(email, request))
    }

    @DeleteMapping("/items/{productId}")
    fun removeItemFromCart(
        @PathVariable productId: Long
    ): ResponseEntity<CartResponseDTO> {
        val email = authenticationFacade.getCurrentUserEmail()
        return ResponseEntity.ok(cartService.removeItemFromCartByUserEmail(email, productId))
    }

    @PostMapping("/checkout")
    fun checkout(
        @RequestBody @Valid paymentRequest: PaymentRequestDTO
    ): ResponseEntity<OrderResponseDTO> {
        val email = authenticationFacade.getCurrentUserEmail()
        return ResponseEntity.ok(
            orderService.checkOutUserByEmail(email = email, paymentRequest = paymentRequest)
        )
    }
}