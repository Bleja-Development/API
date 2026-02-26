package com.makebleja

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
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
import org.jetbrains.exposed.v1.jdbc.Database
import java.util.Properties
import java.io.FileInputStream

fun Application.configureDatabases() {
    val properties = Properties()
    val properiesFile = "local.properties"

    val inputStream = FileInputStream(properiesFile)
    properties.load(inputStream)

    Database.connect(
        url = properties.getProperty("db.url") ?: "dummy_url",
        driver = "org.postgresql.Driver",
        user = properties.getProperty("db.user") ?: "dummy_user",
        password = properties.getProperty("db.password") ?: "dummy_password"
    )
}

