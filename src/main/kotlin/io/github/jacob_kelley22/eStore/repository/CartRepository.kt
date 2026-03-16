package io.github.jacob_kelley22.eStore.repository

import io.github.jacob_kelley22.eStore.entity.Cart
import io.github.jacob_kelley22.eStore.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CartRepository : JpaRepository<Cart, Long> {
    fun findByUserId(userId: Long): Optional<Cart>

    fun findByUser(user: User): Optional<Cart>
}
