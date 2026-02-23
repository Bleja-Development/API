package com.makebleja

import io.ktor.server.application.*

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
    configureRouting()
}
