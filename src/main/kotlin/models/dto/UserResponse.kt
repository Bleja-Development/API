package com.makebleja.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val surname: String,
    val nickname: String,
    val dateOfBirth: String,
    val homeAddress: String

)