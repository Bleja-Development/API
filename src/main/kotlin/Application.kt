package com.makebleja

import com.makebleja.models.ApiResponse
import com.makebleja.models.LoginUserRequest
import com.makebleja.models.RegisterUserRequest
import com.makebleja.models.VerifyCodeRequest
import com.makebleja.services.OtpCleanupService
import com.makebleja.services.UserService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
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
import kotlin.time.Duration.Companion.seconds
import com.makebleja.services.JwtService
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun main(args: Array<String>) {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    io.ktor.server.engine.embeddedServer(
        io.ktor.server.netty.Netty,
        port = port,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
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
    install(io.ktor.server.plugins.cors.routing.CORS) {
        anyHost()
        allowMethod(io.ktor.http.HttpMethod.Options)
        allowMethod(io.ktor.http.HttpMethod.Post)
        allowMethod(io.ktor.http.HttpMethod.Get)
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
        allowHeader(io.ktor.http.HttpHeaders.Authorization)
    }
    configureSerialization()
    val props = configureDatabases()
    launchOtpCleanup()

    val jwtService = JwtService()

    install(Authentication) {

        jwt("auth-jwt") {

            verifier(jwtService.getVerifier())

            validate { credential ->

                val email = credential.payload.getClaim("email").asString()

                if (email != null)
                    JWTPrincipal(credential.payload)
                else
                    null
            }
        }
    }

    install(RateLimit) {
        register(RateLimitName("otp-limit")) {
            rateLimiter(limit = 5, refillPeriod = 60.seconds)
            requestKey { call -> call.request.origin.remoteHost }
        }
    }

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
        validate<VerifyCodeRequest> { request ->
            when{
                request.code.isBlank() -> ValidationResult.Invalid("Incorrect code")
                request.code.length != 6 -> ValidationResult.Invalid("Code must have 6 digits")
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
        status(HttpStatusCode.TooManyRequests) { call, status ->
            val retryAfter = call.response.headers["Retry-After"] ?: "a few"
            val errorResponse = ApiResponse(false, "Too many requests. Wait $retryAfter seconds.")

            call.respondText(
                text = Json.encodeToString(ApiResponse.serializer(), errorResponse),
                contentType = ContentType.Application.Json,
                status = status
            )
        }
    }
}