package com.makebleja

import com.makebleja.services.UserService
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSockets()
    configureFrameworks()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureHTTP()
    val userService = UserService()

    // Pass the service into your routing config
    configureRouting(userService)

    routing {
        swaggerUI(path = "swagger") {
        }
    }
}
