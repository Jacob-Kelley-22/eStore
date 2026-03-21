package io.github.jacob_kelley22.eStore.repository

import io.github.jacob_kelley22.eStore.entity.CheckoutRequest
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface CheckoutRequestRepository : JpaRepository<CheckoutRequest, Long> {
    fun findByUserIdAndIdempotencyKey(userId: Long, idempotencyKey: String): Optional<CheckoutRequest>
}