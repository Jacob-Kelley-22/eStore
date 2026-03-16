package io.github.jacob_kelley22.eStore.dto.user

import io.github.jacob_kelley22.eStore.entity.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserRegistrationRequestDTO(

    @field:Email(message = "Invalid email")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String,

    @field:NotBlank(message = "Role cannot be blank")
    val role: String

)