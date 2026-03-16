package io.github.jacob_kelley22.eStore

import io.github.jacob_kelley22.eStore.dto.product.CreateProductRequestDTO
import io.github.jacob_kelley22.eStore.entity.Product
import io.github.jacob_kelley22.eStore.repository.ProductRepository
import io.github.jacob_kelley22.eStore.service.ProductService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.util.Optional

class ProductServiceTest {

    private val productRepository: ProductRepository = mock()
    private val productService = ProductService(productRepository)

    @Test
    fun `return all products`() {

        val product = Product(
            id = 1,
            name = "Laptop",
            description = "Gaming Laptop",
            price = BigDecimal(1200.0),
            stockQuantity = 5
        )

        whenever(productRepository.findAll()).thenReturn(listOf(product))

        val result = productService.getAllProducts()

        assertEquals(1, result.size)
        assertEquals("Laptop", result[0].name)
    }

    @Test
    fun `admin can update product`() {
        val product = Product(
            id = 1,
            name = "Old Name",
            description = "Old Desc",
            price = BigDecimal(10.00),
            stockQuantity = 5
        )

        val request = CreateProductRequestDTO(
            name = "New Name",
            description = "New Desc",
            price = BigDecimal(9.00),
            stockQuantity = 10
        )

        whenever(productRepository.findById(1)).thenReturn(Optional.of(product))
        whenever(productRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = productService.updateProduct(1, request)

        assertEquals("New Name", result.name)
        assertEquals(BigDecimal(9.00), result.price)
    }

    @Test
    fun `admin can delete product`() {
        val product = Product(
            id = 1,
            name = "Mouse",
            description = "Gaming Mouse",
            price = BigDecimal(50.00),
            stockQuantity = 5
        )

        whenever(productRepository.findById(1)).thenReturn(Optional.of(product))

        productService.deleteProduct(1)

        verify(productRepository).delete(product)
    }

}