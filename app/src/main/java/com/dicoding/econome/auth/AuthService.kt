package com.dicoding.econome.auth

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    @POST("/auth/register")
    fun register(@Body request: RegisterRequest
    ): Call<RegisterResponse>

    @POST("/auth/login")
    fun login(@Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("/auth/refresh-token")
    fun refreshToken(@Body request: RefreshTokenRequest
    ): Call<RefreshTokenResponse>

    @POST("/auth/logout")
    fun logout(@Header("Authorization") token: String
    ): Call<LogoutResponse>
}
