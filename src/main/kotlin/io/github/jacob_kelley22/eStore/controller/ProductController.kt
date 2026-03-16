package io.github.jacob_kelley22.eStore.controller

import io.github.jacob_kelley22.eStore.dto.product.CreateProductRequestDTO
import io.github.jacob_kelley22.eStore.dto.product.ProductResponseDTO
import io.github.jacob_kelley22.eStore.entity.Product
import io.github.jacob_kelley22.eStore.service.ProductService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.multipart.MultipartFile

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
    fun getAllProducts() : List<ProductResponseDTO> {
        return productService.getAllProducts()
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