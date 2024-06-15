package com.dicoding.econome.income

import com.google.gson.annotations.SerializedName

object IncomeRequests {
    data class AddIncomeRequest(
        @SerializedName("date") val date: String,
        @SerializedName("title") val title: String,
        @SerializedName("amount") val amount: Int
    )
}