package io.github.jacob_kelley22.eStore.service

import io.github.jacob_kelley22.eStore.dto.user.UserRegistrationRequestDTO
import io.github.jacob_kelley22.eStore.dto.user.UserResponseDTO
import io.github.jacob_kelley22.eStore.entity.User
import io.github.jacob_kelley22.eStore.mapper.toDTO
import io.github.jacob_kelley22.eStore.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun registerUser(request: UserRegistrationRequestDTO): UserResponseDTO {
        // Check if email is in use
        if (userRepository.findByEmail(request.email).isPresent) {
            throw RuntimeException("User email ${request.email} already exists")
        }

        // Hash pwd
        val password = passwordEncoder.encode(request.password)
            ?: throw IllegalStateException("User password ${request.password} is null or blank")

        // Build user
        val user = User(
            email = request.email,
            password = password
        )

        return userRepository.save(user).toDTO()
    }

    fun findUserByEmail(email: String): UserResponseDTO {
        val user = userRepository.findByEmail(email)
            .orElseThrow  {RuntimeException("User with $email not found") }

        return user.toDTO()
    }

    fun findAllUsers(): List<UserResponseDTO> =
        userRepository.findAll().map { it.toDTO() }

    fun findUserById(id: Long): UserResponseDTO {
        return userRepository.findByIdOrNull(id)?.toDTO()
            ?: throw RuntimeException("User with id $id not found")
    }
}