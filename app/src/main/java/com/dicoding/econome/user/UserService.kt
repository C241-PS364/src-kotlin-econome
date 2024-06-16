package com.dicoding.econome.user

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface UserService {
    @GET("user/profile")
    fun getProfile(@Header("Authorization") token: String): Call<ProfileResponse>
}