package io.github.jacob_kelley22.eStore.entity

import jakarta.persistence.*

@Entity
@Table(name = "carts")
data class Cart(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<CartItem> = mutableListOf()
)
