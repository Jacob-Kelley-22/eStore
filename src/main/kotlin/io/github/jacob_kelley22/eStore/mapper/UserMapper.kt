package io.github.jacob_kelley22.eStore.mapper

import io.github.jacob_kelley22.eStore.dto.user.UserResponseDTO
import io.github.jacob_kelley22.eStore.entity.User

fun User.toDTO() : UserResponseDTO {
    return UserResponseDTO(
        id = this.id,
        email = this.email
    )
}