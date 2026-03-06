package com.makebleja

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import java.util.Properties
import java.io.FileInputStream

fun Application.configureDatabases(): Properties {
    val properties = Properties()
    val propertiesFile = "/etc/secrets/local.properties"

    val inputStream = FileInputStream(propertiesFile)
    properties.load(inputStream)

    Database.connect(
        url = properties.getProperty("db.url") ?: "dummy_url",
        driver = "org.postgresql.Driver",
        user = properties.getProperty("db.user") ?: "dummy_user",
        password = properties.getProperty("db.password") ?: "dummy_password"
    )

    return properties
}
