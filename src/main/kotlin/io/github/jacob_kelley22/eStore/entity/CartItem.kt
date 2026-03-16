package io.github.jacob_kelley22.eStore.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Min

@Entity
@Table(name = "cart_item")
data class CartItem(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    var cart: Cart,

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    val product : Product,

    @field:Min(1)
    var quantity: Int
)
