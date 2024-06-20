package com.dicoding.econome.expense

import android.util.Log
import com.dicoding.econome.activity.expense.ExpenseResponses
import com.dicoding.econome.database.AppDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExpenseRepository(
    private val expenseService: ExpenseService,
    private val database: AppDatabase
) {
    fun addExpense(
        token: String,
        request: ExpenseRequests.AddExpenseRequest,
        callback: (ExpenseResponses.AddExpenseResponse?, String?) -> Unit
    ) {
        Log.d("Expense", "Adding expense with token: $token") // Log the token
        Log.d("Expense", "Request: $request") // Log the request
        expenseService.addExpense(token, request)
            .enqueue(object : Callback<ExpenseResponses.AddExpenseResponse> {
                override fun onResponse(
                    call: Call<ExpenseResponses.AddExpenseResponse>,
                    response: Response<ExpenseResponses.AddExpenseResponse>
                ) {
                    Log.d("Expense", "Response code: ${response.code()}")
                    if (response.isSuccessful) {
                        Log.d("Expense", "Response: ${response.body()}")
                        callback(response.body(), null)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.d("Expense", "Error: $errorBody")
                        callback(null, "Failed to add expense")
                    }
                }

                override fun onFailure(
                    call: Call<ExpenseResponses.AddExpenseResponse>,
                    t: Throwable
                ) {
                    Log.d("Expense", "Failure: ${t.message}")
                    callback(null, t.message)
                }
            })
    }


    fun deleteExpense(
        token: String,
        id: Int,
        callback: (ExpenseResponses.DeleteExpenseResponse?, String?) -> Unit
    ) {
        Log.d("Expense", "Deleting expense with token: $token") // Log the token
        expenseService.deleteExpense(token, id)
            .enqueue(object : Callback<ExpenseResponses.DeleteExpenseResponse> {
                override fun onResponse(
                    call: Call<ExpenseResponses.DeleteExpenseResponse>,
                    response: Response<ExpenseResponses.DeleteExpenseResponse>
                ) {
                    Log.d("Expense", "Response code: ${response.code()}")
                    if (response.isSuccessful) {
                        Log.d("Expense", "Response: ${response.body()}")
                        callback(response.body(), null)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.d("Expense", "Error: $errorBody")
                        callback(null, "Failed to delete expense")
                    }
                }

                override fun onFailure(
                    call: Call<ExpenseResponses.DeleteExpenseResponse>,
                    t: Throwable
                ) {
                    Log.d("Expense", "Failure: ${t.message}")
                    callback(null, t.message)
                }
            })
    }
}