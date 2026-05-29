package io.github.jacob_kelley22.eStore.security

import io.github.jacob_kelley22.eStore.service.CustomUserDetailsService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath
        val method = request.method

        val isPublicProductRead =
            method == "GET" && path.startsWith("/api/products")

        return path.startsWith("/api/auth") ||
                isPublicProductRead ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path == "/swagger-ui.html" ||
                path == "/actuator"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val jwt = authHeader.substring(7)
        val username = jwtUtil.extractUsername(jwt)

        if(SecurityContextHolder.getContext().authentication == null) {

            println("Auth header present")
            println("JWT username: $username")

            val userDetails = userDetailsService.loadUserByUsername(username)
            println("Loaded authorities: ${userDetails.authorities}")

            val valid = jwtUtil.validateToken(jwt, userDetails)
            println("JWT valid: $valid")

            if(valid) {

                val authToken = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )

                authToken.details =
                    WebAuthenticationDetailsSource().buildDetails(request)


                println("Setting auth: ${authToken.authorities}")

                println("JWT authenticated user=$username authorities=${userDetails.authorities}")
                SecurityContextHolder.getContext().authentication = authToken
            }
        }

        filterChain.doFilter(request, response)
    }

}