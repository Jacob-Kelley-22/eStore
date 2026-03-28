package io.github.jacob_kelley22.eStore.controller

import io.github.jacob_kelley22.eStore.dto.product.CreateProductRequestDTO
import io.github.jacob_kelley22.eStore.dto.product.ProductResponseDTO
import io.github.jacob_kelley22.eStore.service.ProductService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.multipart.MultipartFile
import org.springframework.data.domain.Page
import java.math.BigDecimal

@Tag(name = "Product", description = "Product management endpoints")
@RestController
@RequestMapping("/api/products")
class ProductController (private val productService: ProductService) {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createProduct(@RequestBody request: CreateProductRequestDTO): ProductResponseDTO {
        return productService.createProduct(request)
    }

    @GetMapping
    fun getAllProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "asc") sortDirection: String,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) minPrice: BigDecimal?,
        @RequestParam(required = false) maxPrice: BigDecimal?
    ) : Page<ProductResponseDTO> {
        return productService.getAllProducts(
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection,
            name = name,
            minPrice = minPrice,
            maxPrice = maxPrice
        )
    }

    @GetMapping("/{id}")
    fun getProductById(
        @PathVariable id: Long
    ) : ProductResponseDTO? {
        return productService.getProductById(id)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestBody request : CreateProductRequestDTO
    ): ResponseEntity<ProductResponseDTO> {
        val updatedProduct = productService.updateProduct(id, request)
        return ResponseEntity.ok(updatedProduct)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        productService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/image", consumes = ["multipart/form-data"])
    @PreAuthorize("hasRole('ADMIN')")
    fun uploadProductImage(
        @PathVariable id: Long,
        @RequestPart("file") file: MultipartFile
    ): ResponseEntity<ProductResponseDTO> {
        return ResponseEntity.ok(productService.uploadProductImage(id, file))
    }
}