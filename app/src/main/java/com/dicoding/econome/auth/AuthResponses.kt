package com.dicoding.econome.auth

import com.google.gson.annotations.SerializedName

class AuthResponses {
    data class RegisterResponse(
        val error: Boolean,
        val message: String
    )

    data class LoginResponse(
        @SerializedName("error") val error: Boolean,
        @SerializedName("message") val message: String,
        @SerializedName("data") val data: UserData
    )

    data class UserData(
        @SerializedName("userId") val userId: String,
        @SerializedName("name") val name: String,
        @SerializedName("token") val token: String,
        @SerializedName("refreshToken") val refreshToken: String
    )

    data class RefreshTokenResponse(
        val error: Boolean,
        val token: String
    )

    data class LogoutResponse(
        val error: Boolean,
        val message: String
    )
}