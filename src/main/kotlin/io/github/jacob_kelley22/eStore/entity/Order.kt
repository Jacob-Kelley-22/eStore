package io.github.jacob_kelley22.eStore.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.collections.MutableList

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<OrderItem> = mutableListOf(),

    @field:Min(0)
    val totalPrice: BigDecimal = BigDecimal.ZERO,

    @Column(insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null
) {

    fun calculateTotalPrice(): BigDecimal {
        return items.fold(BigDecimal.ZERO) { acc, item ->
            acc + item.priceAtPurchase.multiply(BigDecimal(item.quantity))
        }
    }
}