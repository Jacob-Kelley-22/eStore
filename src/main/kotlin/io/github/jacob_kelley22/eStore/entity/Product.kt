package io.github.jacob_kelley22.eStore.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "products")
class Product (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, length = 1000)
    var description: String?,

    @Column(nullable = false)
    var price: BigDecimal,

    @Column(name = "stock_quantity", nullable = false)
    var stockQuantity: Int,

    @Column(name = "image_url")
    var imageUrl: String? = null,

    @Column(insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)