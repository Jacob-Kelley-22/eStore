package io.github.jacob_kelley22.eStore.service

// DTO
import io.github.jacob_kelley22.eStore.dto.order.OrderResponseDTO
import io.github.jacob_kelley22.eStore.dto.payment.PaymentRequestDTO

// Entity
import io.github.jacob_kelley22.eStore.entity.Order
import io.github.jacob_kelley22.eStore.entity.OrderItem
import io.github.jacob_kelley22.eStore.entity.OrderStatus
import io.github.jacob_kelley22.eStore.entity.User
import io.github.jacob_kelley22.eStore.entity.CheckoutRequest
import io.github.jacob_kelley22.eStore.entity.CheckoutRequestStatus

// Exception
import io.github.jacob_kelley22.eStore.exception.BadRequestException
import io.github.jacob_kelley22.eStore.exception.ForbiddenException
import io.github.jacob_kelley22.eStore.exception.ResourceNotFoundException
import org.springframework.dao.DataIntegrityViolationException

// Repo
import io.github.jacob_kelley22.eStore.repository.OrderRepository
import io.github.jacob_kelley22.eStore.repository.ProductRepository
import io.github.jacob_kelley22.eStore.repository.UserRepository
import io.github.jacob_kelley22.eStore.repository.CartRepository

// Function
import io.github.jacob_kelley22.eStore.mapper.toDTO
import io.github.jacob_kelley22.eStore.repository.CheckoutRequestRepository

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
    private val paymentService: PaymentService,
    private val checkoutRequestRepository: CheckoutRequestRepository
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

        // Validate cart
        val userId = user.id

        // Check for existing request
        val existingRequest = checkoutRequestRepository.findByUserIdAndIdempotencyKey(
            userId, paymentRequest.idempotencyKey
        )

        // If request exists
        if (existingRequest.isPresent) {
            val checkoutRequest = existingRequest.get() // Get it

            // See if the order was completed, return it if it was
            if (checkoutRequest.status == CheckoutRequestStatus.COMPLETED && checkoutRequest.order != null) {
                logger.info(
                    "Returning existing completed checkout for user {} and idempotency key {}",
                    user.email,
                    paymentRequest.idempotencyKey
                )
                return checkoutRequest.order!!.toDTO()
            }

            if (checkoutRequest.status == CheckoutRequestStatus.PENDING) {
                logger.warn(
                    "Duplicate pending checkout blocked for user {} and idempotency key {}",
                    user.email,
                    paymentRequest.idempotencyKey
                    )
                throw BadRequestException("Checkout is already being processed for this idempotency key")
            }

        }

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

        // Validate stock
        // Check to make sure enough stock is present to fulfill the order
        cart.items.forEach { cartItem ->
            val product = cartItem.product
            if (cartItem.quantity > product.stockQuantity) {
                logger.warn(
                    "Insufficient stock for product {}: requested {}, available {}",
                    product.name, cartItem.quantity, product.stockQuantity
                )
                throw BadRequestException(
                    "Insufficient stock for product ${product.name}: " +
                            "requested ${cartItem.quantity}, available ${product.stockQuantity}"
                )
            }
        }

        // Now that we know we can complete the order, start preparing it

        // Build a CheckoutRequest first to prevent two orders being made
        val checkoutRequest = try {
            checkoutRequestRepository.save(
                CheckoutRequest(
                    user = user,
                    idempotencyKey = paymentRequest.idempotencyKey,
                    status = CheckoutRequestStatus.PENDING
                )
            )
        } catch (ex: DataIntegrityViolationException) { // Catch concurrent requests race condition
           logger.warn(
               "Duplicate checkout request detected for user {} and idempotency key {}",
               user.email,
               paymentRequest.idempotencyKey
           )

            // Grab the record for the existing checkout request
            val existing = checkoutRequestRepository
                .findByUserIdAndIdempotencyKey(user.id, paymentRequest.idempotencyKey)
                .orElseThrow {
                    IllegalStateException("CheckoutRequest exists but could not be retrieved")
                }

            // See if it's complete. If so, return it
            if (existing.status == CheckoutRequestStatus.COMPLETED && existing.order != null) {
                logger.info(
                    "Returning existing completed checkout after duplicate insert for user {}",
                    user.email
                )
                return existing.order!!.toDTO()
            }

            // Throw an exception to say the checkout is being processed
            if (existing.status == CheckoutRequestStatus.PENDING) {
                throw BadRequestException("Checkout is already being processed for this idempotency key")
            }

            throw BadRequestException("Checkout is already being processed for this idempotency key")
        }

        // Dummy order used to build final order later
        val order = Order(
            user = user,
            items = mutableListOf(),
            status = OrderStatus.PENDING_PAYMENT,
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

        }

        val finalOrder = Order(
            id = order.id,
            user = order.user,
            items = order.items,
            status = order.status,
            totalPrice = totalPrice
        )

        // Swap order items to point to finalOrder
        finalOrder.items.forEach { it.order = finalOrder }

        // Save the order to the db
        val savedOrder = orderRepository.save(finalOrder)
        logger.info("Order {} has been placed. Processing payment.", savedOrder.id)

        // Process the payment
        // TO-DO: Change payment status to failed if it fails when transaction rollback is fixed
        paymentService.processPayment(savedOrder, paymentRequest)

        savedOrder.status = OrderStatus.PAID // JPA will detect change

        checkoutRequest.status = CheckoutRequestStatus.COMPLETED
        checkoutRequest.order = savedOrder
        checkoutRequest.completedAt = java.time.LocalDateTime.now()

        // Decrement stock of purchased items
        cart.items.forEach { cartItem ->
            val product = cartItem.product
            product.stockQuantity -= cartItem.quantity
            productRepository.save(product)
        }

        // Clear cart after the order is saved, payment is processed, and stock is adjusted
        cart.items.clear()
        cartRepository.save(cart)

        logger.info("Checkout completed for user {}. Order {} has been placed successfully", user.email, savedOrder.id)

        return savedOrder.toDTO()
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