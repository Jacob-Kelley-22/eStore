package io.github.jacob_kelley22.eStore.repository

import io.github.jacob_kelley22.eStore.entity.Cart
import io.github.jacob_kelley22.eStore.entity.User
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CartRepository : JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = ["items", "items.product"])
    fun findByUserId(userId: Long): Optional<Cart>

    @EntityGraph(attributePaths = ["items", "items.product"])
    fun findByUser(user: User): Optional<Cart>
}
