package com.dicoding.econome.user

data class ProfileResponse(
    val error: Boolean,
    val message: String,
    val data: UserData
)