package com.dicoding.econome.user

import com.dicoding.econome.auth.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT

interface UserService {
    @GET("user/profile")
    fun getProfile(@Header("Authorization") token: String): Call<ProfileResponse>

    @PUT("user/profile")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Body profileUpdateRequest: UserResponse.UpdateProfileRequest
    ): Call<UserResponse.UpdateProfileResponse>
}