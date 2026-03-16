package io.github.jacob_kelley22.eStore.controller

import io.github.jacob_kelley22.eStore.dto.user.UserRegistrationRequestDTO
import io.github.jacob_kelley22.eStore.dto.user.UserResponseDTO
import io.github.jacob_kelley22.eStore.service.UserService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "User", description = "User endpoints")
@RestController
@RequestMapping("/users")
class UserController (
    private val userService: UserService
) {

    @PostMapping("/register")
    fun registerUser(
        @RequestBody user : UserRegistrationRequestDTO
    ) : UserResponseDTO {

        return userService.registerUser(user)
    }
}