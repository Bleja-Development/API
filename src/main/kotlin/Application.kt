package com.makebleja

import com.makebleja.models.ApiResponse
import com.makebleja.models.LoginUserRequest
import com.makebleja.models.RegisterUserRequest
import com.makebleja.services.OtpCleanupService
import com.makebleja.services.UserService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.launchOtpCleanup(){
    val cleanupService = OtpCleanupService()

    launch(Dispatchers.IO){
        while(isActive){
            try{
                cleanupService.cleanOtpCodes()
            }catch(e: Exception){
                println("ERROR: Cleanup failed: ${e.message}")
            }
            delay(20.minutes)
        }
    }
}
fun Application.module() {
    configureSerialization()
    val props = configureDatabases()
    launchOtpCleanup()

    val userService = UserService(props)
    configureRouting(userService)

    install(RequestValidation) {
        validate<RegisterUserRequest> { request ->
            when {
                request.email.isBlank() -> ValidationResult.Invalid("Email is required")
                request.password.isBlank() -> ValidationResult.Invalid("Password is required")
                request.password.length < 8 -> ValidationResult.Invalid("Password needs to be at least 8 characters long")
                request.name.isBlank() -> ValidationResult.Invalid("Name is required")
                request.surname.isBlank() -> ValidationResult.Invalid("Surname is required")
                request.dateOfBirth.isBlank() -> ValidationResult.Invalid("Date of birth is required")
                request.homeAddress.isBlank() -> ValidationResult.Invalid("Home address is required")
                request.phoneNumber.isBlank() -> ValidationResult.Invalid("Phone number is required")
                else -> ValidationResult.Valid
            }
        }
        validate<LoginUserRequest> { request ->
            when {
                request.email.isBlank() -> ValidationResult.Invalid("Email is required")
                request.password.isBlank() -> ValidationResult.Invalid("Password is required")
                else -> ValidationResult.Valid
            }
        }
    }
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            val errorResponse = ApiResponse(false, cause.reasons.joinToString())
            call.respondText(
                text = Json.encodeToString(ApiResponse.serializer(), errorResponse),
                contentType = ContentType.Application.Json,
                status = HttpStatusCode.BadRequest
            )
        }
    }
}