package com.dicoding.econome.auth

data class ProfileResponse(
    val error: Boolean,
    val message: String,
    val data: ProfileData
)

data class ProfileData(
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

data class UpdateProfileRequest(
    val name: String,
    val username: String,
    val gender: String,
    val major: String,
    val age: Int
)

data class UpdateProfileResponse(
    val error: Boolean,
    val message: String
)
