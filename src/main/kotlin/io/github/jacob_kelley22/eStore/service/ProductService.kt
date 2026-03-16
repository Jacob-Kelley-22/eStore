package io.github.jacob_kelley22.eStore.service

import io.github.jacob_kelley22.eStore.dto.product.CreateProductRequestDTO
import io.github.jacob_kelley22.eStore.dto.product.ProductResponseDTO
import io.github.jacob_kelley22.eStore.entity.Product
import io.github.jacob_kelley22.eStore.mapper.toDTO
import io.github.jacob_kelley22.eStore.repository.ProductRepository
import io.github.jacob_kelley22.eStore.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@Service
class ProductService (
    private val productRepository: ProductRepository
) {

    fun createProduct(request: CreateProductRequestDTO) : ProductResponseDTO {
        val product = Product(
            name = request.name,
            description = request.description,
            price = request.price,
            stockQuantity = request.stockQuantity
        )
        return productRepository.save(product).toDTO()
    }

    fun getAllProducts(): List<ProductResponseDTO> {
        return productRepository.findAll().map { product -> product.toDTO() }
    }

    fun getProductById(id: Long): ProductResponseDTO {
        val product = productRepository.findById(id)
                .orElseThrow { ResourceNotFoundException("Product with id $id not found") }

        return product.toDTO()
    }

    fun updateProduct(id: Long, request: CreateProductRequestDTO) : ProductResponseDTO {
        val product = productRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("Product with id $id not found") }

        product.name = request.name
        product.description = request.description
        product.price = request.price
        product.stockQuantity = request.stockQuantity

        return productRepository.save(product).toDTO()
    }

    fun deleteProduct(id: Long) {
        val product = productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product with id $id not found") }
        productRepository.delete(product)
    }

    @Transactional
    fun uploadProductImage(
        productId: Long,
        file: MultipartFile
    ): ProductResponseDTO {

        val product = productRepository.findById(productId)
            .orElseThrow { ResourceNotFoundException("Product with id ${productId} not found") }

        val uploadDir = Paths.get("uploads")

        if(!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir)
        }

        val fileName = "${UUID.randomUUID()}_${file.originalFilename}"

        val filePath = uploadDir.resolve(fileName)

        Files.copy(file.inputStream, filePath)

        product.imageUrl = "/uploads/$fileName"

        productRepository.save(product)

        return product.toDTO()
    }

}