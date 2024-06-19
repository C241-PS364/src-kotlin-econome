package com.dicoding.econome.prediction

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface PredictionService {
    @GET("predict/monthly-expense")
    fun getMonthlyExpensePrediction(@Header("Authorization") token: String): Call<PredictionResponse>
}