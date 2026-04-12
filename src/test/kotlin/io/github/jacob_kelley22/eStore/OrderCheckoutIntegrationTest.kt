package io.github.jacob_kelley22.eStore

import io.github.jacob_kelley22.eStore.dto.payment.PaymentRequestDTO
import io.github.jacob_kelley22.eStore.entity.Cart
import io.github.jacob_kelley22.eStore.entity.CartItem
import io.github.jacob_kelley22.eStore.entity.CheckoutRequestStatus
import io.github.jacob_kelley22.eStore.entity.OrderStatus
import io.github.jacob_kelley22.eStore.entity.PaymentStatus
import io.github.jacob_kelley22.eStore.entity.Product
import io.github.jacob_kelley22.eStore.entity.Role
import io.github.jacob_kelley22.eStore.entity.User
import io.github.jacob_kelley22.eStore.repository.CartRepository
import io.github.jacob_kelley22.eStore.repository.CheckoutRequestRepository
import io.github.jacob_kelley22.eStore.repository.OrderRepository
import io.github.jacob_kelley22.eStore.repository.PaymentRepository
import io.github.jacob_kelley22.eStore.repository.ProductRepository
import io.github.jacob_kelley22.eStore.repository.UserRepository
import io.github.jacob_kelley22.eStore.service.OrderService
import io.github.jacob_kelley22.eStore.exception.BadRequestException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class OrderCheckoutIntegrationTest : AbstractPostgresIntegrationTest() {

    @Autowired
    lateinit var orderRepository: OrderRepository
    @Autowired
    lateinit var checkoutRequestRepository: CheckoutRequestRepository

    @Autowired
    lateinit var cartRepository: CartRepository

    @Autowired
    lateinit var orderService: OrderService

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var productRepository: ProductRepository

    @Autowired
    lateinit var paymentRepository: PaymentRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    private lateinit var user: User
    private lateinit var product: Product
    private lateinit var cart: Cart

    @BeforeEach
    fun setUp() {
        paymentRepository.deleteAll()
        checkoutRequestRepository.deleteAll()
        orderRepository.deleteAll()
        cartRepository.deleteAll()
        productRepository.deleteAll()
        userRepository.deleteAll()

        user = userRepository.save(
            User(
                email = "checkout@test.com",
                password = passwordEncoder.encode("password123")!!,
                role = Role.USER
            )
        )

        product = productRepository.save(
            Product(
                name = "Laptop",
                description = "Engineering Laptop",
                price = BigDecimal("1200.00"),
                stockQuantity = 5
            )
        )

        cart = cartRepository.save(
            Cart(
                user = user,
                items = mutableListOf()
            )
        )

        val cartItem = CartItem(
            cart = cart,
            product = product,
            quantity = 2
        )

        cart.items.add(cartItem)
        cartRepository.save(cart)
    }

    @Test
    fun `successful checkout created paid order payment and clears cart`() {
        val request = PaymentRequestDTO(
            idempotencyKey = "checkout-key-12345",
            cardNumber = "4111 1111 1111 1111",
            cardHolderName = "Card Holder",
            expirationMonth = 12,
            expirationYear = 2030,
            cvv = "123"
        )

        val result = orderService.checkout(user, request)

        assertNotNull(result.id)
        assertEquals(user.id, result.userId)
        assertEquals(OrderStatus.PAID, result.status)
        assertEquals(BigDecimal("2400.00"), result.totalPrice)
        assertEquals(1, result.items.size)

        val savedPayments = paymentRepository.findAll()
        assertEquals(1, savedPayments.size)
        assertEquals(PaymentStatus.SUCCEEDED, savedPayments.first().status)
        assertEquals("1111", savedPayments.first().last4)

        val updatedCart = cartRepository.findByUserId(user.id).orElseThrow()
        assertTrue(updatedCart.items.isEmpty())

        val updatedProduct = productRepository.findById(product.id).orElseThrow()
        assertEquals(3, updatedProduct.stockQuantity)

        val checkoutRequests = checkoutRequestRepository.findAll()
        assertEquals(1, checkoutRequests.size)
        assertEquals(
            io.github.jacob_kelley22.eStore.entity.CheckoutRequestStatus.COMPLETED,
            checkoutRequests.first().status
        )
    }

    @Test
    fun `checkout fails for empty cart`() {
        cart.items.clear()
        cartRepository.save(cart)

        val request = PaymentRequestDTO(
            idempotencyKey = "empty-cart-key-12345",
            cardNumber = "4111111111111111",
            cardHolderName = "Card Holder",
            expirationMonth = 12,
            expirationYear = 2030,
            cvv = "123"
        )

        val exception = assertThrows(BadRequestException::class.java) {
            orderService.checkout(user, request)
        }

        assertEquals("Cannot check out with an empty cart", exception.message)
        assertTrue(orderRepository.findAll().isEmpty())
        assertTrue(paymentRepository.findAll().isEmpty())
    }

    @Test
    fun `checkout fails for insufficient stock`() {
        product.stockQuantity = 1
        productRepository.save(product)

        val request = PaymentRequestDTO(
            idempotencyKey = "low-stock-key-12345",
            cardNumber = "4111 1111 1111 1111",
            cardHolderName = "Card Holder",
            expirationMonth = 12,
            expirationYear = 2030,
            cvv = "123"
        )

        val exception = assertThrows(BadRequestException::class.java) {
            orderService.checkout(user, request)
        }

        assertTrue(exception.message!!.contains("Insufficient stock"))
        assertTrue(orderRepository.findAll().isEmpty())
        assertTrue(paymentRepository.findAll().isEmpty())

        val updatedCart = cartRepository.findByUserId(user.id).orElseThrow()
        assertEquals(1, updatedCart.items.size)

        val updatedProduct = productRepository.findById(product.id).orElseThrow()
        assertEquals(1, updatedProduct.stockQuantity)
    }

    @Test
    fun `same idempotency key returns existing completed order`() {
        val request = PaymentRequestDTO(
            idempotencyKey = "same-key-12345",
            cardNumber = "4111 1111 1111 1111",
            cardHolderName = "Card Holder",
            expirationMonth = 12,
            expirationYear = 2030,
            cvv = "123"
        )

        val firstResult = orderService.checkout(user, request)
        val secondResult = orderService.checkout(user, request)

        assertEquals(firstResult.id, secondResult.id)

        assertEquals(1, orderRepository.findAll().size)
        assertEquals(1, paymentRepository.findAll().size)
        assertEquals(1, checkoutRequestRepository.findAll().size)
    }

    @Test
    fun `checkout fails for invalid card and preserves cart and stock`() {
        val request = PaymentRequestDTO(
            idempotencyKey = "invalid-card-key",
            cardNumber = "1234 5678 9012 3456", // invalid Luhn
            cardHolderName = "Card Holder",
            expirationMonth = 12,
            expirationYear = 2030,
            cvv = "123"
        )

        // Check out with bad card
        val exception = assertThrows(BadRequestException::class.java) {
            orderService.checkout(user, request)
        }

        // Check exception
        assertEquals("Invalid card number", exception.message)

        // Check failed order
        val orders = orderRepository.findAll()
        assertEquals(1, orders.size)
        assertEquals(OrderStatus.PAYMENT_FAILED, orders.first().status)
        assertEquals(BigDecimal("2400.00"), orders.first().totalPrice)

        // Check that cart is intact
        val updatedCart = cartRepository.findByUserId(user.id).orElseThrow()
        assertEquals(1, updatedCart.items.size)
        assertEquals(2, updatedCart.items.first().quantity)

        // Stock should NOT be decremented
        val updatedProduct = productRepository.findById(product.id).orElseThrow()
        assertEquals(5, updatedProduct.stockQuantity)

        // Check failed checkout request
        val checkoutRequests = checkoutRequestRepository.findAll()
        assertEquals(1, checkoutRequests.size)
        assertEquals(
            CheckoutRequestStatus.FAILED,
            checkoutRequests.first().status)

    }

    @Test
    fun `checkout fails when idempotency key is already pending`() {
        val request = PaymentRequestDTO(
            idempotencyKey = "pending-key",
            cardNumber = "4111 1111 1111 1111",
            cardHolderName = "Card Holder",
            expirationMonth = 12,
            expirationYear = 2030,
            cvv = "123"
        )

        // First call should succeed
        orderService.checkout(user, request)

        // Manually set request back to PENDING to simulate race condition
        val checkoutRequest = checkoutRequestRepository.findAll().first()
        checkoutRequest.status = CheckoutRequestStatus.PENDING
        checkoutRequestRepository.save(checkoutRequest)

        val exception = assertThrows(BadRequestException::class.java) {
            orderService.checkout(user, request)
        }

        assertTrue(exception.message!!.contains("already being processed"))
    }

    @Test
    fun `checkout fails for expired card and preserves cart and stock`() {
        val request = PaymentRequestDTO(
            idempotencyKey = "test-key",
            cardNumber = "4111 1111 1111 1111",
            cardHolderName = "Card Holder",
            expirationMonth = 1,
            expirationYear = 2020,
            cvv = "123"
        )

        // Submit request with expired card
        val exception = assertThrows(BadRequestException::class.java) {
            orderService.checkout(user, request)
        }

        // Validate exception message
        assertEquals("Card is expired", exception.message)

        // Check that order was created and payment failed
        val orders = orderRepository.findAll()
        assertEquals(1, orders.size)
        assertEquals(OrderStatus.PAYMENT_FAILED, orders.first().status)
        assertEquals(BigDecimal("2400.00"), orders.first().totalPrice)

        // Check that cart is intact
        val updatedCart = cartRepository.findByUserId(user.id).orElseThrow()
        assertEquals(1, updatedCart.items.size)
        assertEquals(2, updatedCart.items.first().quantity)

        // Stock should not be decremented
        val updatedProduct = productRepository.findById(product.id).orElseThrow()
        assertEquals(5, updatedProduct.stockQuantity)

        // Check that failed payment is recorded
        val payments = paymentRepository.findAll()
        assertEquals(1, payments.size)
        assertEquals(PaymentStatus.FAILED, payments.first().status)
        assertEquals("Card is expired", payments.first().failureReason)

        // Check that CheckoutRequest is marked failed
        val checkoutRequests = checkoutRequestRepository.findAll()
        assertEquals(1, checkoutRequests.size)
        assertEquals(
            CheckoutRequestStatus.FAILED,
            checkoutRequests.first().status
        )

    }

    @Test
    fun `failed checkout with same idempotency key can be retried successfully`() {
        val failedRequest = PaymentRequestDTO(
            idempotencyKey = "retry-key",
            cardNumber = "4111 1111 1111 1111",
            cardHolderName = "Card Holder",
            expirationMonth = 1,
            expirationYear = 2020,
            cvv = "123"
        )

        assertThrows(BadRequestException::class.java) {
            orderService.checkout(user, failedRequest)
        }

        val failedOrder = orderRepository.findAll().first()
        assertEquals(OrderStatus.PAYMENT_FAILED, failedOrder.status)

        val retryRequest = PaymentRequestDTO(
            idempotencyKey = "retry-key",
            cardNumber = "4111 1111 1111 1111",
            cardHolderName = "Card Holder",
            expirationMonth = 12,
            expirationYear = 2030,
            cvv = "123"
        )

        val result = orderService.checkout(user, retryRequest)

        assertEquals(failedOrder.id, result.id)
        assertEquals(OrderStatus.PAID, result.status)

        val allOrder = orderRepository.findAll()
        assertEquals(1, allOrder.size)
        assertEquals(OrderStatus.PAID, allOrder.first().status)

        val payments = paymentRepository.findAll()
        assertEquals(2, payments.size)
        assertTrue(payments.any { it.status == PaymentStatus.FAILED })
        assertTrue(payments.any { it.status == PaymentStatus.SUCCEEDED })

        val updatedCart = cartRepository.findByUserId(user.id).orElseThrow()
        assertTrue(updatedCart.items.isEmpty())
    }

}