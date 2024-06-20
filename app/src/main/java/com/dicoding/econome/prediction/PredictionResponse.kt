package com.dicoding.econome.prediction

data class PredictionResponse(
    val error: Boolean,
    val message: String,
    val predictedExpense: Float
)