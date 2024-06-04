package com.dicoding.econome.util

import com.google.gson.annotations.SerializedName

data class ApiResponse(

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null,
)