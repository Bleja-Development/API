package com.makebleja.routes

import com.makebleja.models.ApiResponse
import com.makebleja.models.LoginUserRequest
import com.makebleja.models.RegisterUserRequest
import com.makebleja.models.VerifyCodeRequest
import com.makebleja.services.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userService: UserService) {
    route("/users"){
        get("/test") { call.respond(userService.test() ?: HttpStatusCode.NotFound) }
        post("/register") {
            val request = call.receive<RegisterUserRequest>()

            if (userService.getUserByEmail(request.email) != null) {
                return@post call.respond(HttpStatusCode.Conflict, ApiResponse(false, "Email is already taken"))
            }

            val newUser = userService.registerUser(request)
            call.respond(HttpStatusCode.Created, ApiResponse(true, "Success! Please verify your email next.", newUser))
        }
        post("/verify"){
            val request = call.receive<VerifyCodeRequest>()
            val result = userService.verifyAccount(request)
            val status = if (result.success) HttpStatusCode.OK else HttpStatusCode.BadRequest
            call.respond(status, result)
        }
        post("/login") {
            val request = call.receive<LoginUserRequest>()
            val result = userService.logInUser(request)
            val isVerified = userService.isVerified(request.email)

            if(isVerified){
                if (result != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        result
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse(success = false, message = "Invalid email or password")
                    )
                }
            }else call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "You need to verify your account first!"))

        }
        authenticate("auth-jwt") {

            get("/me") {

                val principal = call.principal<JWTPrincipal>()
                val email = principal!!.payload.getClaim("email").asString()
                val user = userService.getUserByEmail(email)

                call.respond(ApiResponse(true, "Authenticated as $email", user))
            }
        }
        rateLimit(RateLimitName("otp-limit")){
            post("/resend-otp") {
                val email = call.receiveParameters()["email"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                try {
                    userService.resendOTP(email)
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "A new code has been sent to your email."))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse(false, "Could not resend code: $e"))
                }
            }
        }
    }
}