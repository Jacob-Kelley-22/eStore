package io.github.jacob_kelley22.eStore.repository

import io.github.jacob_kelley22.eStore.entity.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface OrderRepository : JpaRepository<Order, Long>{
    fun findByUserId(userId: Long, pageable: Pageable): Page<Order>

    fun findByIdAndUserId(orderId: Long, userId: Long): Optional<Order>
}