package com.dicoding.econome.income

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface IncomeService {

    @POST("incomes")
    fun addIncome(
        @Header("Authorization") token: String,
        @Body request: IncomeRequests.AddIncomeRequest
    ): Call<IncomeResponses.AddIncomeResponse>

    @DELETE("incomes/{id}")
    fun deleteIncome(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<IncomeResponses.DeleteIncomeResponse>
}