package com.dicoding.econome.auth

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {

    @POST("auth/register")
    fun register(@Body request: AuthRequests.RegisterRequest): Call<AuthResponses.RegisterResponse>

    @POST("auth/login")
    fun login(@Body request: AuthRequests.LoginRequest): Call<AuthResponses.LoginResponse>

    @POST("auth/refresh-token")
    fun refreshToken(@Body request: AuthRequests.RefreshTokenRequest): Call<AuthResponses.RefreshTokenResponse>

    @POST("auth/logout")
    fun logout(@Header("Authorization") token: String): Call<AuthResponses.LogoutResponse>
}