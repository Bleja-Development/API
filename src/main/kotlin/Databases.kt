package com.makebleja

import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.config.HoconApplicationConfig
import org.jetbrains.exposed.v1.jdbc.Database

fun Application.configureDatabases() {
    val config = HoconApplicationConfig(ConfigFactory.load())

    val dbUser = config.propertyOrNull("postgres.user")?.getString() ?: ""
    val dbUrl = config.propertyOrNull("postgres.url")?.getString() ?: ""
    val dbPassword = config.propertyOrNull("postgres.password")?.getString() ?: ""

    Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPassword
    )
}
