package io.github.jacob_kelley22.eStore.service

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class AuthenticationFacade {
    // Helper to get current user email

    fun getCurrentUserEmail(): String {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw RuntimeException("No authentication found")

        return authentication.name
    }

}