package io.github.jacob_kelley22.eStore.dto.auth

data class LoginResponseDTO(
    val token: String,
    val userId: Long,
    val email: String,
    val role: String
)