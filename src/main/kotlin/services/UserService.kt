package com.makebleja.services

import com.makebleja.entities.Users
import com.makebleja.models.ApiResponse
import com.makebleja.models.LoginResponse
import com.makebleja.models.LoginUserRequest
import com.makebleja.models.RegisterUserRequest
import com.makebleja.models.UserResponse
import com.makebleja.models.VerifyCodeRequest
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Properties

class UserService(props: Properties){
    private val emailService = EmailService(props)
    fun test(): UserResponse? = transaction {
        Users.selectAll()
            .map { row ->
                UserResponse(
                    id = row[Users.id].toString(),
                    email = row[Users.email],
                    name = row[Users.name],
                    surname = row[Users.surname],
                    nickname = row[Users.nickname],
                    dateOfBirth = row[Users.dateOfBirth].toString(),
                    homeAddress = row[Users.homeAddress],
                    phoneNumber = row[Users.phoneNumber],
                    verified = row[Users.verified]
                )
            }.singleOrNull()
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
                    phoneNumber = row[Users.phoneNumber],
                    verified = row[Users.verified]
                )
            }.singleOrNull()
    }
    fun registerUser(request: RegisterUserRequest): UserResponse = transaction {
        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
        val newUser = Users.insert {
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

        UserResponse(
            id = newUser[Users.id].toString(),
            email = newUser[Users.email],
            name = newUser[Users.name],
            surname = newUser[Users.surname],
            nickname = newUser[Users.nickname],
            dateOfBirth = newUser[Users.dateOfBirth].toString(),
            homeAddress = newUser[Users.homeAddress],
            phoneNumber = newUser[Users.phoneNumber],
            verified = newUser[Users.verified]
        )
    }
    fun resendOTP(email: String) = transaction {
        val newCode = (100000..999999).random().toString()
        val newExpiry = Instant.now().plus(5, ChronoUnit.MINUTES)

        val existing = OtpCodes.selectAll().where { OtpCodes.email eq email }.singleOrNull()

        if(existing != null){
            OtpCodes.update({ OtpCodes.email eq email}){
                it[code] = newCode
                it[expiresAt] = newExpiry
            }
        }else{
            OtpCodes.insert{
                it[OtpCodes.email] = email
                it[code] = newCode
                it[expiresAt] = newExpiry
            }
        }

        emailService.sendOtpCode(email, newCode)
    }
    fun verifyAccount(request: VerifyCodeRequest): ApiResponse = transaction {
        val otpRow = OtpCodes
            .selectAll()
            .where { OtpCodes.email eq request.email }
            .singleOrNull() ?: return@transaction ApiResponse(false, "Code not found")

        val dbCode = otpRow[OtpCodes.code]
        val expiry = otpRow[OtpCodes.expiresAt]
        val now = Instant.now()
        val currentAttempts = otpRow[OtpCodes.attempts]

        if(expiry.isBefore(now)){
            OtpCodes.deleteWhere { OtpCodes.email eq request.email }
            return@transaction ApiResponse(false, "Code expired")
        }

        if(dbCode == request.code){
            OtpCodes.deleteWhere { OtpCodes.email eq request.email }
            Users.update(where = { Users.email eq request.email }){
                it[verified] = true
                it[updatedAt] = now
            }
            return@transaction ApiResponse(true, "Account verified!")
        } else{
            val newAttempts = currentAttempts + 1

            if(newAttempts >= 3){
                OtpCodes.deleteWhere { OtpCodes.email eq request.email }
                return@transaction ApiResponse(false, "Too many failed attempts, request a new code")
            } else{
                OtpCodes.update({ OtpCodes.email eq request.email }) {
                    it[attempts] = newAttempts
                }
                return@transaction ApiResponse(false, "Invalid code, you have ${3-newAttempts} attempts left")
            }
        }
    }
    fun isVerified(email: String): Boolean = transaction {
        val verified = Users.select(Users.verified).where{ Users.email eq email }.map{ it[Users.verified] }.singleOrNull() ?: false
        verified
    }

    private val jwtService = JwtService()

    fun logInUser(request: LoginUserRequest): LoginResponse? = transaction {
        val userRow = Users.selectAll()
            .where { Users.email eq request.email }
            .singleOrNull() ?: return@transaction null

        val passwordMatches = BCrypt.checkpw(request.password, userRow[Users.password])

        if (!passwordMatches) return@transaction null

        val user = UserResponse(
            id = userRow[Users.id].toString(),
            email = userRow[Users.email],
            name = userRow[Users.name],
            surname = userRow[Users.surname],
            nickname = userRow[Users.nickname],
            dateOfBirth = userRow[Users.dateOfBirth].toString(),
            homeAddress = userRow[Users.homeAddress],
            phoneNumber = userRow[Users.phoneNumber],
            verified = userRow[Users.verified]
        )

        val token = jwtService.generateToken(user.id, user.email)

        LoginResponse(true, "Login successful!", token, user)
    }
}