package com.makebleja.routes

import com.makebleja.models.ApiResponse
import com.makebleja.models.LoginUserRequest
import com.makebleja.models.RegisterUserRequest
import com.makebleja.services.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
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
            val answer = userService.logInUser(request)

            call.respond(HttpStatusCode.OK, answer)
        }
    }
}