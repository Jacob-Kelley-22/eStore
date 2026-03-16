package io.github.jacob_kelley22.eStore.service

// DTO
import io.github.jacob_kelley22.eStore.dto.order.OrderResponseDTO
import io.github.jacob_kelley22.eStore.dto.payment.PaymentRequestDTO

// Entity
import io.github.jacob_kelley22.eStore.entity.Order
import io.github.jacob_kelley22.eStore.entity.OrderItem
import io.github.jacob_kelley22.eStore.entity.User
import io.github.jacob_kelley22.eStore.exception.BadRequestException
import io.github.jacob_kelley22.eStore.exception.ForbiddenException
import io.github.jacob_kelley22.eStore.exception.ResourceNotFoundException

// Repo
import io.github.jacob_kelley22.eStore.repository.OrderRepository
import io.github.jacob_kelley22.eStore.repository.ProductRepository
import io.github.jacob_kelley22.eStore.repository.UserRepository
import io.github.jacob_kelley22.eStore.repository.CartRepository

// Function
import io.github.jacob_kelley22.eStore.mapper.toDTO

// Annotation
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal

// Logging
import org.slf4j.LoggerFactory

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val paymentService: PaymentService
) {

    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    fun getAllOrders(): List<OrderResponseDTO> {
        return orderRepository.findAll().map {
            it.toDTO()
        }
    }

    fun getOrderById(id: Long): OrderResponseDTO {

        return orderRepository.findById(id)
            .orElseThrow {
                logger.error("Order with id $id not found")
                ResourceNotFoundException("Order with id $id not found")
            }
            .toDTO()

    }

    @Transactional
    fun checkout(
        user: User,
        paymentRequest: PaymentRequestDTO
    ): OrderResponseDTO {

        val userId = user.id

        // Start by getting cart from db. User is already found
        logger.info("Checking out user {}", user.email)

        val cart = cartRepository.findByUserId(userId)
            .orElseThrow {
                logger.warn("Cart for user {} not found while checking out", user.email)
                ResourceNotFoundException("Cart with id $userId not found")
            }

        // Make sure items are in cart. Otherwise, the order shouldn't be made
        if (cart.items.isEmpty()) {
            logger.warn("Cart belonging to user $userId was empty while checking out")
            throw BadRequestException("Cannot check out with an empty cart")
        }

        // Check to make sure enough stock is present to fulfill the order
        cart.items.forEach { cartItem ->
            val product = cartItem.product
            if (cartItem.quantity > product.stockQuantity) {
                logger.warn(
                    "Insufficient stock for product {}: requested {}, available {}",
                    cartItem.product, cartItem.quantity, product.stockQuantity
                )
                throw BadRequestException(
                    "Insufficient stock for product ${cartItem.product.name}: " +
                            "requested ${cartItem.quantity}, available ${product.stockQuantity}"
                )
            }
        }

        // Now that we know we can complete the order, start preparing it

        // Process the payment
        val paymentResponse = paymentService.processPayment(paymentRequest)

        if (!paymentResponse.approved) {
            logger.warn("Payment for user {} not approved for payment", userId)
            throw BadRequestException("Payment was declined")
        }

        // Dummy order used to build final order later
        val order = Order(
            user = user,
            items = mutableListOf(),
            totalPrice = BigDecimal.ZERO
        )

        // Running total for final order total
        var totalPrice = BigDecimal.ZERO

        // Convert cart items into order items
        cart.items.forEach { cartItem ->
            val product = cartItem.product

            val orderItem = OrderItem(
                order = order,
                product = product,
                quantity = cartItem.quantity,
                priceAtPurchase = product.price
            )

            // Add converted order item to dummy order
            order.items.add(orderItem)

            // Get the line amount of the cart item (price * quantity) and add it to the total
            val lineTotal = product.price.multiply(BigDecimal(cartItem.quantity))
            totalPrice = totalPrice.add(lineTotal)

            // Decrease stock quantity for items in cart
            product.stockQuantity -= cartItem.quantity
            productRepository.save(product)
        }

        val finalOrder = Order(
            id = order.id,
            user = order.user,
            items = order.items,
            totalPrice = totalPrice
        )

        // Swap order items to point to finalOrder
        finalOrder.items.forEach { it.order = finalOrder }

        // Save the order to the db
        val savedOrder = orderRepository.save(finalOrder).toDTO()
        logger.info("User {} has completed checkout. Order {} has been placed.", user.email, finalOrder.id)

        // Clear cart after the order is saved!
        cart.items.clear()
        cartRepository.save(cart)

        return savedOrder
    }

    @Transactional
    fun checkOutUserByEmail(
        email: String,
        paymentRequest: PaymentRequestDTO
    ): OrderResponseDTO {
        val user = userRepository.findByEmail(email)
            .orElseGet {
                logger.warn("User with email $email not found while checking out.")
                throw ResourceNotFoundException("User with email $email not found")
            }

        return checkout(user = user, paymentRequest = paymentRequest)
    }

    // Get all of a user's orders
    fun getOrdersByUserEmail(email: String): List<OrderResponseDTO> {
        val user = userRepository.findByEmail(email)
            .orElseThrow {
                logger.warn("User with email $email not found while fetching their orders.")
                ResourceNotFoundException("User with email $email not found")
            }

        return orderRepository.findByUserId(user.id).map { it.toDTO() }
    }

    // Get a specific order for a user, only letting them see their order
    fun getOrderByIdForUser(email: String, orderId: Long): OrderResponseDTO {
        // Find user
        val user = userRepository.findByEmail(email)
            .orElseThrow {
                logger.warn("User with email $email not found while fetching their order.")
                ResourceNotFoundException("User with email $email not found")
            }

        val order = orderRepository.findByIdAndUserId(orderId = orderId, userId = user.id)
            .orElseThrow {
                logger.warn("Order with id {} does not belong to user {}", orderId, user.id)
                throw ForbiddenException("Access denied to order $orderId")
            }

        return order.toDTO()
    }
}