package com.dicoding.econome.util

import android.content.Context
import com.dicoding.econome.auth.ApiConfig
import com.dicoding.econome.database.AppDatabase

object Injection {
    fun provideRepo(context: Context): Repository {
        val apiService = ApiConfig.api
        val database = AppDatabase.getDatabase(context)
        return Repository(apiService, database)
    }
}