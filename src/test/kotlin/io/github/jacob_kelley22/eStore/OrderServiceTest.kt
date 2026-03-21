package io.github.jacob_kelley22.eStore

import io.github.jacob_kelley22.eStore.entity.*
import io.github.jacob_kelley22.eStore.repository.*
import io.github.jacob_kelley22.eStore.service.OrderService
import io.github.jacob_kelley22.eStore.service.PaymentService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import org.mockito.kotlin.*
import java.math.BigDecimal


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

    /*@Test
    fun `createOrder returns correct OrderResponseDTO`(){

        //Arrange
        val user = User(
            id = 1,
            password = "test",
            email = "test@test.com"
        )

        val product1 = Product(
            id = 1,
            name = "Laptop",
            description = "Gaming Laptop",
            price = BigDecimal(1000),
            quantity = 5
        )

        val product2 = Product(
            id = 2,
            name = "Mouse",
            description = "Gaming Mouse",
            price = BigDecimal(50),
            quantity = 5
        )

        val request = CreateOrderRequestDTO(
            userId = 1,
            items = listOf(
                CreateOrderItemRequestDTO(productId = product1.id, quantity = 1),
                CreateOrderItemRequestDTO(productId = product2.id, quantity = 2)
            )
        )

        whenever(userRepo.findById(1)).thenReturn(Optional.of(user))
        whenever(productRepo.findById(1)).thenReturn(Optional.of(product1))
        whenever(productRepo.findById(2)).thenReturn(Optional.of(product2))

        whenever(orderRepo.save(any())).thenAnswer { it.arguments[0] }

        // Act
        val response = orderService.createOrder(request)

        // Assert
        assertEquals(1, response.userId)
        assertEquals(BigDecimal(1100), response.totalPrice)
        assertEquals(2, response.items.size)
        assertEquals(1, response.items[0].productId)
        assertEquals(2, response.items[1].productId)

        verify(orderRepo, times(1)).save(any())
    }

    @Test
    fun `createOrder throws exception when product not found`() {

        val user = User(
            id = 1,
            email = "test@test.com",
            password = "test"
            )

        val request = CreateOrderRequestDTO(
            userId = 1,
            items = listOf(CreateOrderItemRequestDTO(productId = 1, quantity = 1))
        )

        whenever(userRepo.findById(1)).thenReturn(Optional.of(user))
        whenever(productRepo.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows<RuntimeException> {
            orderService.createOrder(request)
        }

        assertEquals("Product 1 not found", exception.message)
    }*/

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