package com.dicoding.econome.auth

class AuthRequests {
    data class RegisterRequest(
        val username: String,
        val password: String,
        val name: String,
        val gender: String,
        val major: String,
        val age: Int
    )

    data class LoginRequest(
        val username: String,
        val password: String
    )

    data class RefreshTokenRequest(
        val refreshToken: String
    )
}