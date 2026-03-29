package io.github.jacob_kelley22.eStore

import io.github.jacob_kelley22.eStore.dto.payment.PaymentRequestDTO
import io.github.jacob_kelley22.eStore.entity.Cart
import io.github.jacob_kelley22.eStore.entity.CartItem
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

}