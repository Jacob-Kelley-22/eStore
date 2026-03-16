package io.github.jacob_kelley22.eStore.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Claims
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import java.nio.charset.StandardCharsets
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil {

    // Generate HMAC-SHA key for signing JWTs
    private val secret = "Secret-Key-That-Cannot-Be-Used-In-Live-Environments-1234567890"
    private val key = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    // Expiration time
    private val expiry = 1000 * 60 * 60 * 10 // 10 hours

    fun generateToken(email: String, role: String): String {
        return Jwts.builder()
            .subject(email)
            .claim("role", role)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiry)) // 1 hour
            .signWith(key)
            .compact()
    }

    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
    }

    fun extractRole(token: String): String {
        return extractAllClaims(token)["role"] as String
    }

    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractAllClaims(token).expiration.before(Date())
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }


}