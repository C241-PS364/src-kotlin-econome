package com.dicoding.econome.expense

import com.dicoding.econome.activity.expense.ExpenseResponses
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ExpenseService {

    @POST("expenses")
    fun addExpense(
        @Header("Authorization") token: String,
        @Body request: ExpenseRequests.AddExpenseRequest
    ): Call<ExpenseResponses.AddExpenseResponse>

    @DELETE("expenses/{id}")
    fun deleteExpense(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<ExpenseResponses.DeleteExpenseResponse>
}