package com.dicoding.econome.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dicoding.econome.auth.UserResponse.ProfileData
import com.dicoding.econome.auth.UserResponse.UpdateProfileRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingPreference private constructor(private val dataStore: DataStore<Preferences>) {

    private val TOKEN = stringPreferencesKey("token")
    private val USER_ID = stringPreferencesKey("userId")
    private val NAME = stringPreferencesKey("name")
    private val USERNAME = stringPreferencesKey("username")
    private val GENDER = stringPreferencesKey("gender")
    private val MAJOR = stringPreferencesKey("major")
    private val AGE = stringPreferencesKey("age")
    private val STATUS = booleanPreferencesKey("status")

    fun getUserData(): Flow<ProfileData> {
        return dataStore.data.map { preferences ->
            ProfileData(
                id = preferences[USER_ID]?.toInt() ?: 0,
                uuid = preferences[USER_ID] ?: "",
                name = preferences[NAME] ?: "",
                username = preferences[USERNAME] ?: "",
                gender = preferences[GENDER] ?: "",
                major = preferences[MAJOR] ?: "",
                age = preferences[AGE]?: "",
                token = preferences[TOKEN] ?: "", // Add this line
                created_at = "",
                updated_at = ""
            )
        }
    }

    suspend fun putUserData(user: UpdateProfileRequest?) {
        dataStore.edit { preferences ->
            preferences[NAME] = user!!.name
            preferences[USERNAME] = user.username
            preferences[GENDER] = user.gender
            preferences[MAJOR] = user.major
            preferences[AGE] = user.age.toString()
        }
    }

    suspend fun updateProfileData(profile: ProfileData) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = profile.uuid
            preferences[NAME] = profile.name
            preferences[USERNAME] = profile.username
            preferences[GENDER] = profile.gender
            preferences[MAJOR] = profile.major
            preferences[AGE] = profile.age.toString()
        }
    }

    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingPreference? = null

        fun getInstance(dataStore: DataStore<Preferences>): SettingPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}
