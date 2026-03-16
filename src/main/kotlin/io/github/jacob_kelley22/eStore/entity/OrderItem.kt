package io.github.jacob_kelley22.eStore.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

@Entity
data class OrderItem(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "order_id")
    var order: Order?,

    @ManyToOne
    @JoinColumn(name = "product_id")
    val product: Product,

    @field:Min(1)
    val quantity: Int,

    val priceAtPurchase: BigDecimal
)
