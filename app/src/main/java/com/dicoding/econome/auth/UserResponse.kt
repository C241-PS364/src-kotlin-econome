package com.dicoding.econome.auth

import com.google.gson.annotations.SerializedName

class UserResponse {
    data class ProfileResponse(
        @SerializedName("error") val error: Boolean,
        @SerializedName("message") val message: String,
        @SerializedName("data") val data: ProfileData
    )

    data class ProfileData(
        @SerializedName("id") val id: Int,
        @SerializedName("uuid") val uuid: String,
        @SerializedName("name") val name: String,
        @SerializedName("username") val username: String,
        @SerializedName("gender") val gender: String,
        @SerializedName("major") val major: String,
        @SerializedName("age") val age: Int,
        @SerializedName("created_at") val created_at: java.sql.Timestamp,
        @SerializedName("updated_at") val updated_at: java.sql.Timestamp,
        @SerializedName("token") val token: String,
    )

    data class UpdateProfileRequest(
        val username: String,
        val name: String,
        val gender: String,
        val major: String,
        val age: Int
    )

    data class UpdateProfileResponse(
        @SerializedName("error") val error: Boolean,
        @SerializedName("message") val message: String
    )
}