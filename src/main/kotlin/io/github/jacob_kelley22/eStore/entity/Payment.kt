package io.github.jacob_kelley22.eStore.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "payments",
    indexes = [
        Index(name = "idx_payment_order_id", columnList = "order_id"),
        Index(name = "idx_payment_public_id", columnList = "public_id"),
        Index(name = "idx_payment_transaction_id", columnList = "transaction_id"),

    ])
data class Payment (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    val publicId: UUID = UUID.randomUUID(),

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,

    @Column(nullable = false, unique = true, updatable = false)
    val transactionId: String,

    @Column(nullable = false, updatable = false)
    val cardHolderName: String,

    @Column(nullable = false, length = 4, updatable = false)
    val last4: String,

    @Column(nullable = false, precision = 19, scale = 2, updatable = false)
    val amount: BigDecimal,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null, // DB assigns value on insert

    @Column
    var processedAt: LocalDateTime? = null,

    @Column(length = 255)
    var failureReason: String? = null

)
