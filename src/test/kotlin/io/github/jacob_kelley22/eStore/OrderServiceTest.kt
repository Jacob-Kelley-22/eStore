package io.github.jacob_kelley22.eStore

import io.github.jacob_kelley22.eStore.entity.*
import io.github.jacob_kelley22.eStore.exception.ForbiddenException
import io.github.jacob_kelley22.eStore.exception.ResourceNotFoundException
import io.github.jacob_kelley22.eStore.repository.*
import io.github.jacob_kelley22.eStore.service.OrderService
import io.github.jacob_kelley22.eStore.service.PaymentService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import org.mockito.kotlin.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

@ActiveProfiles("test")
@SpringBootTest
class OrderServiceTest {
    private val orderRepo: OrderRepository = mock()
    private val userRepo: UserRepository = mock()
    private val productRepo: ProductRepository = mock()
    private val cartRepo: CartRepository = mock()
    private val paymentRepo: PaymentRepository = mock()
    private val checkoutRequestRepository: CheckoutRequestRepository = mock()
    private val paymentService = PaymentService(
        paymentRepo
    )
    private val orderService = OrderService(
        orderRepo,
        userRepo,
        productRepo,
        cartRepo,
        paymentService,
        checkoutRequestRepository
    )

    @Test
    fun `get orders by user email returns paginated order responses` () {
        val user = User(
            id = 1,
            email = "test@test.com",
            password = "encoded-password"
        )

        val product = Product(
            id = 10,
            name = "Laptop",
            description = "Gaming Laptop",
            price = BigDecimal("1200.00"),
            stockQuantity = 5
        )

        val order = Order(
            id  = 100,
            user = user,
            items = mutableListOf(),
            totalPrice = BigDecimal("1200.00"),
            status = OrderStatus.PAID
        )

        val orderItem = OrderItem(
            id = 1000,
            order = order,
            product = product,
            quantity = 1,
            priceAtPurchase = BigDecimal("1200.00")
        )

        order.items.add(orderItem)

        whenever(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user))

        whenever(orderRepo.findByUserId(eq(1L), any())).thenReturn(PageImpl(listOf(order)))

        val result = orderService.getOrdersByUserEmail(
            email = "test@test.com",
            page = 0,
            size = 10,
            sortBy = "createdAt",
            sortDirection = "desc"
        )

        assertEquals(1, result.content.size)
        assertEquals(100L, result.content[0].id)
        assertEquals(1L, result.content[0].userId)
        assertEquals(BigDecimal("1200.00"), result.content[0].totalPrice)

        verify(userRepo).findByEmail("test@test.com")
        verify(orderRepo).findByUserId(
            eq(1L),
            any()
        )
    }
    @Test
    fun `get orders by user email throws when user not found`() {
        whenever(userRepo.findByEmail("missing@test.com")).thenReturn(Optional.empty())

        val exception = assertThrows<ResourceNotFoundException> {
            orderService.getOrdersByUserEmail(
                email = "missing@test.com",
                page = 0,
                size = 10,
                sortBy = "createdAt",
                sortDirection = "desc"
            )
        }

        assertEquals("User with email missing@test.com not found", exception.message)
    }

    @Test
    fun `get order by id for user returns correct order`() {
        val user = User(
            id = 1,
            email = "test@test.com",
            password = "encoded-password"
        )

        val product = Product(
            id = 10,
            name = "Laptop",
            description = "Gaming Laptop",
            price = BigDecimal("1200.00"),
            stockQuantity = 5
        )

        val order = Order(
            id = 100,
            user = user,
            items = mutableListOf(),
            totalPrice = BigDecimal("1200.00"),
            status = OrderStatus.PAID
        )

        val orderItem = OrderItem(
            id = 1000,
            order = order,
            product = product,
            quantity = 1,
            priceAtPurchase = BigDecimal("1200.00")
        )

        order.items.add(orderItem)

        whenever(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user))
        whenever(orderRepo.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(order))

        val result = orderService.getOrderByIdForUser("test@test.com", 100L)

        assertEquals(100L, result.id)
        assertEquals(1L, result.userId)
        assertEquals(1, result.items.size)
    }

    @Test
    fun `get order by id for user throws forbidden when order does not belong to user`() {
        val user = User(
            id = 1,
            email = "test@test.com",
            password = "encoded-password"
        )

        whenever(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user))
        whenever(orderRepo.findByIdAndUserId(100L, 1L)).thenReturn(Optional.empty())

        val exception = assertThrows<ForbiddenException> {
            orderService.getOrderByIdForUser("test@test.com", 100L)
        }

        assertEquals("Access denied to order 100", exception.message)
    }

    @Test
    fun `getOrderById returns correct OrderResponseDTO`() {

        val user = User(
            id = 1,
            password = "test",
            email = "test@test.com"
        )

        val product = Product(
            id = 1,
            name = "Laptop",
            description = "Gaming Laptop",
            price = BigDecimal(1000.0),
            stockQuantity = 5
        )

        val order = Order(
            id = 1,
            user = user,
            items = mutableListOf(),
            totalPrice = BigDecimal(1000.0),
        )

        val orderItem = OrderItem(
            id = 1,
            order = order,
            product = product,
            quantity = 1,
            priceAtPurchase = product.price
        )

        order.items.add(orderItem)
        whenever(orderRepo.findById(1)).thenReturn(Optional.of(order))

        val response = orderService.getOrderById(1)

        assertEquals(1, response.id)
        assertEquals(BigDecimal(1000.0), response.totalPrice)
        assertEquals(1, response.items.size)
    }

}