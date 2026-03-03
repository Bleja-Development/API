package com.makebleja.models

import kotlinx.serialization.Serializable

@Serializable
data class VerifyCodeRequest (
    val email: String,
    val code: String
)