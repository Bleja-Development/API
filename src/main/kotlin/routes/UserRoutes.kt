package com.makebleja.routes

import com.makebleja.models.ApiResponse
import com.makebleja.models.LoginUserRequest
import com.makebleja.models.RegisterUserRequest
import com.makebleja.services.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userService: UserService) {
    route("/users"){
        get("/getAllUsers") {
            try {
                val users = userService.getAllUsers()
                call.respond(users)
            } catch (e: Exception) {
                call.respondText("Database Error: ${e.message}")
            }
        }
        post("/register") {
            val request = call.receive<RegisterUserRequest>()

            if (userService.getUserByEmail(request.email) != null) {
                return@post call.respond(HttpStatusCode.Conflict, ApiResponse(false, "Email taken"))
            }

            userService.registerUser(request)
            call.respond(HttpStatusCode.Created, ApiResponse(true, "Success!"))
        }
        post("/login") {
            val request = call.receive<LoginUserRequest>()
            val isSuccess = userService.logInUser(request)

            if (isSuccess) {
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, message = "Login successful!")
                )
            } else {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse(success = false, message = "Invalid email or password")
                )
            }
        }
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