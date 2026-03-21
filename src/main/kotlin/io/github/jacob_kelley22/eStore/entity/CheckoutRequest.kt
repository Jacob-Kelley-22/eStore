package io.github.jacob_kelley22.eStore.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(
    name = "checkout_requests",
    uniqueConstraints = [
        UniqueConstraint(
            name = "checkout_request_user_idempotency_key",
            columnNames = ["user_id", "idempotency_key"]
        )
    ],
    indexes = [
        Index(name = "idx_checkout_request_user_id", columnList = "user_id"),
        Index(name = "idx_checkout_request_idempotency_key", columnList = "idempotency_key")
    ]
)
data class CheckoutRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    val user: User,

    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 100)
    val idempotencyKey: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: CheckoutRequestStatus = CheckoutRequestStatus.PENDING,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    var order: Order? = null,

    @Column(length = 255)
    var failureReason: String? = null,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    var completedAt: LocalDateTime? = null


    )
