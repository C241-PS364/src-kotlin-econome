package com.dicoding.econome.auth

data class RegisterResponse(
    val error: Boolean,
    val message: String
)

data class LoginResponse(
    val error: Boolean,
    val message: String,
    val data: LoginResult
)

data class LoginResult(
    val userId: String,
    val name: String,
    val token: String,
    val refreshToken: String
)

data class RefreshTokenResponse(
    val error: Boolean,
    val token: String
)

data class LogoutResponse(
    val error: Boolean,
    val message: String
)
