package io.github.jacob_kelley22.eStore.entity

import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Column
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType

@Entity
@Table(name = "users")
class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Enumerated(EnumType.STRING)
    val role: Role = Role.USER
)

enum class Role {
    USER, ADMIN
}