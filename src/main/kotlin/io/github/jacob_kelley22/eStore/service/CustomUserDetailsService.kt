package io.github.jacob_kelley22.eStore.service

import io.github.jacob_kelley22.eStore.repository.UserRepository
import io.github.jacob_kelley22.eStore.security.CustomUserDetails
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
): UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {

        val user = userRepository.findByEmail(username)
            .orElseThrow { RuntimeException("User with $username not found") }

        println("Loaded user: ${user.email}, role ${user.role}")

        return CustomUserDetails(user)
    }
}