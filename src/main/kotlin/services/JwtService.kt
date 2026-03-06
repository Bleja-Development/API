package com.makebleja.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

class JwtService {
    private val secret = "super-secret"
    private val issuer = "ktor-app"
    private val audience = "ktor-users"
    private val validityInMs = 1000 * 60 * 60 * 24

    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(userId: String, email: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .sign(algorithm)
    }

    fun getVerifier() =
        JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
}