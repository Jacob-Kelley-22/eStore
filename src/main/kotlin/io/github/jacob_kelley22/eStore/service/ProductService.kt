package io.github.jacob_kelley22.eStore.service

import io.github.jacob_kelley22.eStore.dto.product.CreateProductRequestDTO
import io.github.jacob_kelley22.eStore.dto.product.ProductResponseDTO
import io.github.jacob_kelley22.eStore.entity.Product
import io.github.jacob_kelley22.eStore.mapper.toDTO
import io.github.jacob_kelley22.eStore.repository.ProductRepository
import io.github.jacob_kelley22.eStore.exception.ResourceNotFoundException
import io.github.jacob_kelley22.eStore.repository.ProductSpecifications
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@Service
class ProductService(
    private val productRepository: ProductRepository
) {

    fun createProduct(request: CreateProductRequestDTO): ProductResponseDTO {
        val product = Product(
            name = request.name,
            description = request.description,
            price = request.price,
            stockQuantity = request.stockQuantity
        )
        return productRepository.save(product).toDTO()
    }

    fun getAllProducts(
        page: Int,
        size: Int,
        sortBy: String,
        sortDirection: String,
        name: String?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?
    ): Page<ProductResponseDTO> {
        val sort = if (sortDirection.equals("desc", ignoreCase = true)) {
            Sort.by(sortBy).descending()
        } else {
            Sort.by(sortBy).ascending()
        }

        val pageable = PageRequest.of(page, size, sort)

        var spec: Specification<Product>? = null

        if (!name.isNullOrBlank()) {
            spec = if (spec == null) {
                ProductSpecifications.nameContains(name)
            } else {
                spec.and(ProductSpecifications.nameContains(name))
            }
        }

        if (minPrice != null) {
            spec = spec?.and(ProductSpecifications.priceGreaterThanOrEqualTo(minPrice))
                ?: ProductSpecifications.priceGreaterThanOrEqualTo(minPrice)
        }

        if (maxPrice != null) {
            spec = spec?.and(ProductSpecifications.priceLessThanOrEqualTo(maxPrice))
                ?: ProductSpecifications.priceLessThanOrEqualTo(maxPrice)
        }

        return if (spec != null) {
            productRepository.findAll(spec, pageable).map { it.toDTO() }
        } else {
            productRepository.findAll(pageable).map { it.toDTO() }
        }
    }

    fun getProductById(id: Long): ProductResponseDTO {
        val product = productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product with id $id not found") }

        return product.toDTO()
    }

    fun updateProduct(id: Long, request: CreateProductRequestDTO): ProductResponseDTO {
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

        if (!Files.exists(uploadDir)) {
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