package com.makebleja

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import java.util.Properties
import java.io.FileInputStream

fun Application.configureDatabases(): Properties {
    val properties = Properties()
    val dbUrl = System.getenv("DB_URL") ?: ""
    val dbUser = System.getenv("DB_USER") ?: ""
    val dbPass = System.getenv("DB_PASSWORD") ?: ""

    properties.setProperty("url", dbUrl)
    properties.setProperty("user", dbUser)
    properties.setProperty("password", dbPass)

    Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPass
    )

    return properties
}
