package com.makebleja.services

import com.makebleja.models.RegisterUserRequest
import com.makebleja.models.UserResponse
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDate

class UserService {
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
            it[dateOfBirth] = LocalDate.parse(request.dateOfBirth)
            it[homeAddress] = request.homeAddress
            it[password] = hashedPassword
            it[phoneNumber] = request.phoneNumber
        }
    }
}