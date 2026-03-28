package io.github.jacob_kelley22.eStore.repository

import io.github.jacob_kelley22.eStore.entity.Product
import org.springframework.data.jpa.domain.Specification
import java.math.BigDecimal

object ProductSpecifications {

    fun nameContains(name: String): Specification<Product> =
        Specification { root, _, cb ->
            cb.like(cb.lower(root.get("name")), "%${name.lowercase()}%")
        }

    fun priceGreaterThanOrEqualTo(minPrice: BigDecimal): Specification<Product> =
        Specification { root, _, cb ->
            cb.greaterThanOrEqualTo(root.get("price"), minPrice)
        }

    fun priceLessThanOrEqualTo(maxPrice : BigDecimal): Specification<Product> =
        Specification { root, _, cb ->
            cb.lessThanOrEqualTo(root.get("price"), maxPrice)
        }
}