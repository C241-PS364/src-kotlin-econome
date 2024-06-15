package com.dicoding.econome.auth

class UserResponse {
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
        val age: String,
        val created_at: String,
        val updated_at: String,
        val token: String
    )

    data class UpdateProfileRequest(
        val name: String,
        val username: String,
        val gender: String,
        val major: String,
        val age: String
    )

    data class UpdateProfileResponse(
        val error: Boolean,
        val message: String
    )
}