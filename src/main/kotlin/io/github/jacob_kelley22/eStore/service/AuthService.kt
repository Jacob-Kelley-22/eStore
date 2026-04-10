package io.github.jacob_kelley22.eStore.service

import io.github.jacob_kelley22.eStore.dto.auth.LoginRequestDTO
import io.github.jacob_kelley22.eStore.dto.user.UserRegistrationRequestDTO
import io.github.jacob_kelley22.eStore.dto.auth.LoginResponseDTO
import io.github.jacob_kelley22.eStore.entity.Role
import io.github.jacob_kelley22.eStore.entity.User
import io.github.jacob_kelley22.eStore.exception.ResourceNotFoundException
import io.github.jacob_kelley22.eStore.repository.UserRepository
import io.github.jacob_kelley22.eStore.security.JwtUtil
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtUtil = JwtUtil(),
    private val authenticationManager: AuthenticationManager
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    // Register a user
    fun register(request: UserRegistrationRequestDTO): LoginResponseDTO {
        // Check if user already exists

        logger.info("Registering user: ${request.email}")

        if (userRepository.findByEmail(request.email).isPresent) {
            logger.warn("User with email ${request.email} already exists")
            throw RuntimeException("Registration failed " +
                    "because user email ${request.email} already exists")
        }

        // Create new user entity
        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password)!!,
            role = Role.valueOf(request.role.uppercase())
        )

        // Save user to database
        val savedUser = userRepository.save(user)

        logger.info("User {} registered successfully with role {}", savedUser.email, savedUser.role)

        // Generate JWT token
        val token = jwtService.generateToken(email = savedUser.email, role = savedUser.role.name)

        return LoginResponseDTO(
            token = token,
            userId = savedUser.id,
            email = savedUser.email,
            role = savedUser.role.name
        )
    }

    fun login(request: LoginRequestDTO): LoginResponseDTO {
        // Authenticate using auth manager
        val authToken = UsernamePasswordAuthenticationToken(
            request.email,
            request.password
        )
        logger.info("Login attempt for user: {}", request.email)
        authenticationManager.authenticate(authToken)

        // Retrieve user
        val user = userRepository.findByEmail(request.email)
            .orElseThrow {
                logger.warn("User with email ${request.email} not found. Login failed.")
                throw ResourceNotFoundException("User ${request.email} not found")
            }


        logger.info("Login successful for user: {}", request.email)

        val token = jwtService.generateToken(email = user.email, role = user.role.name)

        return LoginResponseDTO(
            token = token,
            userId = user.id,
            email = user.email,
            role = user.role.name

        )
    }
}