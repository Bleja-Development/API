package com.makebleja.routes

import com.makebleja.models.dto.RegisterUserRequest
import com.makebleja.services.UserService
import io.ktor.server.application.*
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
        post("/register"){
            val request = call.receive<RegisterUserRequest>()

            try{
                userService.registerUser(request)
                call.respondText("Registered successfully!", status = io.ktor.http.HttpStatusCode.Created)
            }
            catch (e: Exception) {
                call.respondText("Registration failed: ${e.message}", status = io.ktor.http.HttpStatusCode.BadRequest)
            }
        }
    }
}