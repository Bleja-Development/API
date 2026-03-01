package com.makebleja.services

import com.makebleja.entities.Users
import com.makebleja.models.LoginUserRequest
import com.makebleja.models.RegisterUserRequest
import com.makebleja.models.UserResponse
import io.ktor.server.plugins.di.DependencyInitializer
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Properties
import javax.swing.text.BoxView

class UserService(private val props: Properties){
    private val emailService = EmailService(props)

    fun getAllUsers(): List<UserResponse> = transaction {
        Users.selectAll().map { row ->
            UserResponse(
                id = row[Users.id].toString(),
                email = row[Users.email],
                name = row[Users.name],
                surname = row[Users.surname],
                nickname = row[Users.nickname],
                dateOfBirth = row[Users.dateOfBirth].toString(),
                homeAddress = row[Users.homeAddress],
                phoneNumber = row[Users.phoneNumber]
            )
        }
    }
    fun getUserByEmail(email: String): UserResponse? = transaction {
        Users.selectAll().where{ Users.email eq email }
            .map { row ->
                UserResponse(
                    id = row[Users.id].toString(),
                    email = row[Users.email],
                    name = row[Users.name],
                    surname = row[Users.surname],
                    nickname = row[Users.nickname],
                    dateOfBirth = row[Users.dateOfBirth].toString(),
                    homeAddress = row[Users.homeAddress],
                    phoneNumber = row[Users.phoneNumber]
                )
            }.singleOrNull()
    }
    fun registerUser(request: RegisterUserRequest) = transaction {
        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
        Users.insert {
            it[email] = request.email
            it[name] = request.name
            it[surname] = request.surname
            it[nickname] = request.nickname
            it[dateOfBirth] = java.time.LocalDate.parse(request.dateOfBirth)
            it[homeAddress] = request.homeAddress
            it[password] = hashedPassword
            it[phoneNumber] = request.phoneNumber
        }
        val randomCode = (100000..999999).random()
        OtpCodes.insert {
            it[email] = request.email
            it[code] = randomCode.toString()
            it[expiresAt] = Instant.now().plus(5, ChronoUnit.MINUTES)
        }

        emailService.sendOtpCode(request.email, randomCode.toString())
    }

    fun logInUser(request: LoginUserRequest): String = transaction {

        val passwordFromDatabase =
            Users.select(Users.password)
                .where { Users.email eq request.email }
                .map { it[Users.password] }
                .singleOrNull()

        if (passwordFromDatabase.isNullOrBlank()) {
            return@transaction "User not found"
        }

        val matchedPassword = BCrypt.checkpw(request.password, passwordFromDatabase)

        if(matchedPassword)
            "Matched"
        else
            "Passwords doesn't match"
    }
}