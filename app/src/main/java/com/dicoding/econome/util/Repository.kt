package com.dicoding.econome.util

import android.content.Context
import android.util.Log
import com.dicoding.econome.auth.AuthRequests
import com.dicoding.econome.auth.AuthResponses
import com.dicoding.econome.auth.AuthService
import com.dicoding.econome.database.AppDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Repository(private val authService: AuthService, private val database: AppDatabase) {

    fun login(context: Context, username: String, password: String, callback: (AuthResponses.LoginResponse?, String?) -> Unit) {
        authService.login(AuthRequests.LoginRequest(username, password)).enqueue(object : Callback<AuthResponses.LoginResponse> {
            override fun onResponse(call: Call<AuthResponses.LoginResponse>, response: Response<AuthResponses.LoginResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.data?.token
                    Log.d("Login", "Response: ${response.body()}")
                    Log.d("Login", "Token: $token")

                    // Save the token in SharedPreferences
                    val sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("token", token)
                    editor.apply()

                    // Log the saved token
                    val savedToken = sharedPreferences.getString("token", null)
                    Log.d("Login", "Saved token: $savedToken")

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
        username:String,
        pass: String,
        name: String,
        gender: String,
        major: String,
        age: Int,
        callback: (AuthResponses.RegisterResponse?, String?) -> Unit
    ) {
        val registerRequest = AuthRequests.RegisterRequest(username, pass, name, gender, major, age)
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