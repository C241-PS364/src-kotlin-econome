package com.dicoding.econome.auth

import com.dicoding.econome.expense.ExpenseService
import com.dicoding.econome.income.IncomeService
import com.dicoding.econome.prediction.PredictionService
import com.dicoding.econome.user.UserService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    private const val BASE_URL = "https://econome-api-i2dyjb7xmq-et.a.run.app/api/v1/"

    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val incomeService: IncomeService by lazy {
        retrofit.create(IncomeService::class.java)
    }

    val expenseService: ExpenseService by lazy {
        retrofit.create(ExpenseService::class.java)
    }

    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }

    val predictionService: PredictionService by lazy {
        retrofit.create(PredictionService::class.java)
    }
}