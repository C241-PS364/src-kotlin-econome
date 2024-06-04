package com.dicoding.econome.response

data class UserResponse(
    val token: String,
    val status: Boolean,
    val nama: String,
    val userid: String
)