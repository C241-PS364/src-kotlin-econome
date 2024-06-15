package com.dicoding.econome.income

import com.google.gson.annotations.SerializedName

object IncomeResponses {
    data class IncomeData(
        @SerializedName("id") val id: Int,
        @SerializedName("date") val date: String,
        @SerializedName("title") val title: String,
        @SerializedName("amount") val amount: Int
    )

    data class AddIncomeResponse(
        @SerializedName("error") val error: Boolean,
        @SerializedName("message") val message: String,
        @SerializedName("data") val data: IncomeData
    )

    data class DeleteIncomeResponse(
        @SerializedName("error") val error: Boolean,
        @SerializedName("message") val message: String
    )
}