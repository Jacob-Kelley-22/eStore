package io.github.jacob_kelley22.eStore.controller

import io.github.jacob_kelley22.eStore.dto.auth.LoginRequestDTO
import io.github.jacob_kelley22.eStore.dto.auth.LoginResponseDTO
import io.github.jacob_kelley22.eStore.dto.user.UserRegistrationRequestDTO
import io.github.jacob_kelley22.eStore.dto.user.UserResponseDTO
import io.github.jacob_kelley22.eStore.mapper.toDTO
import io.github.jacob_kelley22.eStore.security.JwtUtil
import io.github.jacob_kelley22.eStore.service.AuthService
import io.github.jacob_kelley22.eStore.service.UserService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Authentication", description = "Authentication endpoints")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    val authService: AuthService
    ) {

    @PostMapping("/register")
    fun registerUser(
        @RequestBody @Valid request: UserRegistrationRequestDTO
    ): ResponseEntity<LoginResponseDTO> {
        // Register user
        val response: LoginResponseDTO = authService.register(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequestDTO): ResponseEntity<LoginResponseDTO> {
        // Authenticate user
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }
}