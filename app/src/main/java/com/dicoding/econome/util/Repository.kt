package com.dicoding.econome.util

import android.util.Log
import com.dicoding.econome.auth.AuthRequests
import com.dicoding.econome.auth.AuthResponses
import com.dicoding.econome.auth.AuthService
import com.dicoding.econome.database.AppDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Repository(private val authService: AuthService, private val database: AppDatabase) {

    fun login(username: String, password: String, callback: (AuthResponses.LoginResponse?, String?) -> Unit) {
        authService.login(AuthRequests.LoginRequest(username, password)).enqueue(object : Callback<AuthResponses.LoginResponse> {
            override fun onResponse(call: Call<AuthResponses.LoginResponse>, response: Response<AuthResponses.LoginResponse>) {
                if (response.isSuccessful) {
                    Log.d("Login", "Response: ${response.body()}")
                    callback(response.body(), null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.d("Login", "Error: $errorBody")
                    callback(null, "Login failed")
                }
            }

            override fun onFailure(call: Call<AuthResponses.LoginResponse>, t: Throwable) {
                Log.d("Login", "Failure: ${t.message}")
                callback(null, t.message)
            }
        })
    }

    fun register(
        name: String,
        email: String,
        pass: String,
        age: Int,
        major: String,
        gender: String,
        callback: (AuthResponses.RegisterResponse?, String?) -> Unit
    ) {
        val registerRequest = AuthRequests.RegisterRequest(name, email, pass, gender, major, age)
        authService.register(registerRequest).enqueue(object : Callback<AuthResponses.RegisterResponse> {
            override fun onResponse(call: Call<AuthResponses.RegisterResponse>, response: Response<AuthResponses.RegisterResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    callback(response.body(), null)
                } else {
                    callback(null, "Registration failed")
                }
            }

            override fun onFailure(call: Call<AuthResponses.RegisterResponse>, t: Throwable) {
                callback(null, t.message)
            }
        })
    }
}
