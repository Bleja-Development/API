package com.makebleja.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserRequest(
    val email: String,
    val name: String,
    val surname: String,
    val nickname: String,
    val dateOfBirth: String,
    val homeAddress: String,
    val password: String,
    val phoneNumber: String
)