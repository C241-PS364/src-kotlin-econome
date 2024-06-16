package com.dicoding.econome.user

data class UserData(
    val id: Int,
    val uuid: String,
    val name: String,
    val username: String,
    val gender: String,
    val major: String,
    val age: Int,
    val created_at: String,
    val updated_at: String
)