package com.dicoding.econome.income

import android.util.Log
import com.dicoding.econome.database.AppDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IncomeRepository(private val incomeService: IncomeService, private val database: AppDatabase) {
    fun addIncome(
        token: String,
        request: IncomeRequests.AddIncomeRequest,
        callback: (IncomeResponses.AddIncomeResponse?, String?) -> Unit
    ) {
        Log.d("Income", "Adding income with token: $token") // Log the token
        Log.d("Income", "Request: $request") // Log the request
        incomeService.addIncome(token, request)
            .enqueue(object : Callback<IncomeResponses.AddIncomeResponse> {
                override fun onResponse(
                    call: Call<IncomeResponses.AddIncomeResponse>,
                    response: Response<IncomeResponses.AddIncomeResponse>
                ) {
                    Log.d("Income", "Response code: ${response.code()}")
                    if (response.isSuccessful) {
                        Log.d("Income", "Response: ${response.body()}")
                        callback(response.body(), null)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.d("Income", "Error: $errorBody")
                        callback(null, "Failed to add income")
                    }
                }

                override fun onFailure(
                    call: Call<IncomeResponses.AddIncomeResponse>,
                    t: Throwable
                ) {
                    Log.d("Income", "Failure: ${t.message}")
                    callback(null, t.message)
                }
            })
    }


    fun deleteIncome(
        token: String,
        id: Int,
        callback: (IncomeResponses.DeleteIncomeResponse?, String?) -> Unit
    ) {
        Log.d("Income", "Deleting income with token: $token") // Log the token
        incomeService.deleteIncome(token, id)
            .enqueue(object : Callback<IncomeResponses.DeleteIncomeResponse> {
                override fun onResponse(
                    call: Call<IncomeResponses.DeleteIncomeResponse>,
                    response: Response<IncomeResponses.DeleteIncomeResponse>
                ) {
                    Log.d("Income", "Response code: ${response.code()}")
                    if (response.isSuccessful) {
                        Log.d("Income", "Response: ${response.body()}")
                        callback(response.body(), null)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.d("Income", "Error: $errorBody")
                        callback(null, "Failed to delete income")
                    }
                }

                override fun onFailure(
                    call: Call<IncomeResponses.DeleteIncomeResponse>,
                    t: Throwable
                ) {
                    Log.d("Income", "Failure: ${t.message}")
                    callback(null, t.message)
                }
            })
    }
}