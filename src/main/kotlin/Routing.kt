package com.makebleja

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.makebleja.routes.userRoutes
import com.makebleja.services.UserService
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.di.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.sql.Connection
import java.sql.DriverManager
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.Serializable

fun Application.configureRouting(userService: UserService) { // Pass the service here
    install(Resources)
    routing {
        // This tells Swagger to look for the file in src/main/resources/openapi/documentation.yaml
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")

        get("/") {
            call.respondRedirect("/swagger")
        }

        // Register your user routes here
        userRoutes(userService)
    }
}