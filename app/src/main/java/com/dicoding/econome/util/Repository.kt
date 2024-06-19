package com.dicoding.econome.util

import android.content.Context
import android.util.Log
import com.dicoding.econome.auth.AuthRequests
import com.dicoding.econome.auth.AuthResponses
import com.dicoding.econome.auth.AuthService
import com.dicoding.econome.auth.UserResponse
import com.dicoding.econome.database.AppDatabase
import com.dicoding.econome.prediction.PredictionResponse
import com.dicoding.econome.prediction.PredictionService
import com.dicoding.econome.user.ProfileResponse
import com.dicoding.econome.user.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Repository(
    private val authService: AuthService,
    private val userService: UserService,
    private val predictionService: PredictionService,
    private val database: AppDatabase
) {

    fun refreshToken(
        context: Context,
        refreshToken: String,
        callback: (AuthResponses.RefreshTokenResponse?, String?) -> Unit
    ) {
        val request = AuthRequests.RefreshTokenRequest(refreshToken)
        authService.refreshToken(request)
            .enqueue(object : Callback<AuthResponses.RefreshTokenResponse> {
                override fun onResponse(
                    call: Call<AuthResponses.RefreshTokenResponse>,
                    response: Response<AuthResponses.RefreshTokenResponse>
                ) {
                    if (response.isSuccessful) {
                        val newAccessToken = response.body()?.token
                        // Save the new access token
                        callback(response.body(), null)
                    } else {
                        // Handle the error
                        callback(null, "Refresh token failed")
                    }
                }

                override fun onFailure(
                    call: Call<AuthResponses.RefreshTokenResponse>,
                    t: Throwable
                ) {
                    // Handle the failure
                    callback(null, t.message)
                }
            })
    }

    fun login(
        context: Context,
        username: String,
        password: String,
        callback: (AuthResponses.LoginResponse?, String?) -> Unit
    ) {
        authService.login(AuthRequests.LoginRequest(username, password))
            .enqueue(object : Callback<AuthResponses.LoginResponse> {
                override fun onResponse(
                    call: Call<AuthResponses.LoginResponse>,
                    response: Response<AuthResponses.LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        val token = response.body()?.data?.token
                        val refreshToken =
                            response.body()?.data?.refreshToken // Get the refresh token

                        Log.d("Login", "Response: ${response.body()}")
                        Log.d("Login", "Token: $token")

                        // Save the token and refresh token in SharedPreferences
                        val sharedPreferences =
                            context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("token", token)
                        editor.putString("refreshToken", refreshToken) // Save the refresh token
                        editor.apply()

                        // Log the saved token and refresh token
                        val savedToken = sharedPreferences.getString("token", null)
                        val savedRefreshToken = sharedPreferences.getString(
                            "refreshToken",
                            null
                        ) // Log the saved refresh token
                        Log.d("Login", "Saved token: $savedToken")
                        Log.d("Login", "Saved refresh token: $savedRefreshToken")

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
        username: String,
        pass: String,
        name: String,
        gender: String,
        major: String,
        age: Int,
        callback: (AuthResponses.RegisterResponse?, String?) -> Unit
    ) {
        val registerRequest = AuthRequests.RegisterRequest(username, pass, name, gender, major, age)
        authService.register(registerRequest)
            .enqueue(object : Callback<AuthResponses.RegisterResponse> {
                override fun onResponse(
                    call: Call<AuthResponses.RegisterResponse>,
                    response: Response<AuthResponses.RegisterResponse>
                ) {
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

    fun logout(context: Context, callback: (String?) -> Unit) {
        val sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        if (token != null) {
            authService.logout("Bearer $token")
                .enqueue(object : Callback<AuthResponses.LogoutResponse> {
                    override fun onResponse(
                        call: Call<AuthResponses.LogoutResponse>,
                        response: Response<AuthResponses.LogoutResponse>
                    ) {
                        if (response.isSuccessful) {
                            // Clear the token from SharedPreferences
                            val editor = sharedPreferences.edit()
                            editor.remove("token")
                            editor.remove("refreshToken")
                            editor.apply()

                            callback(null)
                        } else {
                            callback("Logout failed")
                        }
                    }

                    override fun onFailure(call: Call<AuthResponses.LogoutResponse>, t: Throwable) {
                        callback(t.message)
                    }
                })
        } else {
            callback("No token found")
        }
    }

    fun getProfile(
        context: Context,
        callback: (ProfileResponse?, String?) -> Unit
    ) {
        val sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        if (token != null) {
            userService.getProfile("Bearer $token").enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        callback(response.body(), null)
                    } else {
                        callback(null, "Failed to fetch profile")
                    }
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    callback(null, t.message)
                }
            })
        } else {
            callback(null, "No token found")
        }
    }

    fun updateProfile(
        context: Context,
        username: String,
        name: String,
        gender: String,
        major: String,
        age: Int,
        callback: (UserResponse.UpdateProfileResponse?, String?) -> Unit
    ) {
        val sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        if (token != null) {
            val updateRequest =
                UserResponse.UpdateProfileRequest(username, name, gender, major, age)

            userService.updateProfile("Bearer $token", updateRequest)
                .enqueue(object : Callback<UserResponse.UpdateProfileResponse> {
                    override fun onResponse(
                        call: Call<UserResponse.UpdateProfileResponse>,
                        response: Response<UserResponse.UpdateProfileResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            callback(response.body(), null)
                        } else {
                            callback(null, "Update failed")
                        }
                    }

                    override fun onFailure(
                        call: Call<UserResponse.UpdateProfileResponse>,
                        t: Throwable
                    ) {
                        callback(null, t.message)
                    }
                })
        } else {
            callback(null, "No token found")
        }
    }

    fun getMonthlyExpensePrediction(
        context: Context,
        callback: (Float?, String?) -> Unit
    ) {
        val sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        if (token != null) {
            predictionService.getMonthlyExpensePrediction("Bearer $token")
                .enqueue(object : Callback<PredictionResponse> {
                    override fun onResponse(
                        call: Call<PredictionResponse>,
                        response: Response<PredictionResponse>
                    ) {
                        if (response.isSuccessful) {
                            val prediction = response.body()?.predictedExpense
                            callback(prediction, null)
                        } else {
                            callback(null, "Failed to fetch prediction")
                        }
                    }

                    override fun onFailure(call: Call<PredictionResponse>, t: Throwable) {
                        callback(null, t.message)
                    }
                })
        } else {
            callback(null, "No token found")
        }
    }
}
