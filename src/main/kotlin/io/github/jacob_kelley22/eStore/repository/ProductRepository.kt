package io.github.jacob_kelley22.eStore.repository

import io.github.jacob_kelley22.eStore.entity.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {
}