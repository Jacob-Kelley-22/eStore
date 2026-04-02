package io.github.jacob_kelley22.eStore.controller

import io.github.jacob_kelley22.eStore.dto.order.OrderResponseDTO
import io.github.jacob_kelley22.eStore.service.AuthenticationFacade
import io.github.jacob_kelley22.eStore.service.OrderService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Order", description = "Order endpoints")
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
    private val authenticationFacade: AuthenticationFacade
) {

    @GetMapping
    fun getUserOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDirection: String
    ): ResponseEntity<Page<OrderResponseDTO>> {
        val email = authenticationFacade.getCurrentUserEmail()
        return ResponseEntity.ok(orderService.getOrdersByUserEmail(
            email = email,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection
            )
        )
    }

    @GetMapping("/{orderId}")
    fun getOrderById(
        @PathVariable orderId: Long
    ): ResponseEntity<OrderResponseDTO> {
        val email = authenticationFacade.getCurrentUserEmail()
        return ResponseEntity.ok(orderService.getOrderByIdForUser(email, orderId))
    }
}