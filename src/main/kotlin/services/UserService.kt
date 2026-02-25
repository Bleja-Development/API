package com.makebleja.services

import com.makebleja.models.dto.RegisterUserRequest
import com.makebleja.models.dto.UserResponse
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

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
                homeAddress = row[Users.homeAddress]
            )
        }
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
        }
    }
}