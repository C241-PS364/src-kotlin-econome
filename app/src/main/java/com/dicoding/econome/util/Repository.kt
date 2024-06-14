package com.dicoding.econome.util

import com.dicoding.econome.auth.AuthRequests
import com.dicoding.econome.auth.AuthResponses
import com.dicoding.econome.auth.AuthService
import com.dicoding.econome.database.AppDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Repository(private val authService: AuthService, database: AppDatabase) {

    fun login(email: String, pass: String, callback: (AuthResponses.LoginResponse?) -> Unit) {
        val loginRequest = AuthRequests.LoginRequest(email, pass)
        authService.login(loginRequest).enqueue(object : Callback<AuthResponses.LoginResponse> {
            override fun onResponse(call: Call<AuthResponses.LoginResponse>, response: Response<AuthResponses.LoginResponse>) {
                callback(response.body())
            }

            override fun onFailure(call: Call<AuthResponses.LoginResponse>, t: Throwable) {
                callback(null)
            }
        })
    }

    fun register(
        name: String,
        email: String,
        pass: String,
        age: String,
        major: String,
        gender: String,
        callback: (AuthResponses.AuthResponse?) -> Unit
    ) {
        val registerRequest = AuthRequests.RegisterRequest(name, email, pass, "", gender, major, age.toInt())
        authService.register(registerRequest).enqueue(object : Callback<AuthResponses.AuthResponse> {
            override fun onResponse(call: Call<AuthResponses.AuthResponse>, response: Response<AuthResponses.AuthResponse>) {
                callback(response.body())
            }

            override fun onFailure(call: Call<AuthResponses.AuthResponse>, t: Throwable) {
                callback(null)
            }
        })
    }
}
