package com.dicoding.econome.util

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.dicoding.econome.database.AppDatabase
import com.dicoding.econome.response.LoginResponse
import com.dicoding.econome.response.Result

class Repository private constructor(
    private val apiService: ApiService,
    private val appDatabase: AppDatabase
) {
    private val _user = MutableLiveData<LoginResponse?>()
    private val user: LiveData<LoginResponse?> = _user

    private val _response = MutableLiveData<ApiResponse?>()
    private val response: LiveData<ApiResponse?> = _response


    fun login(email: String, pass: String): LiveData<Result<LoginResponse?>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.postLogin(email, pass)
            _user.value = response

        } catch (e: Exception) {
            Log.d("Repository", "${e.message.toString()} ")
            emit(Result.Error(e.message.toString()))
        }
        val localData: LiveData<Result<LoginResponse?>> = user.map { Result.Success(it) }
        emitSource(localData)
    }

    fun register(
        nama: String,
        email: String,
        pass: String,
        age: String,
        major: String,
        gender: String
    ): LiveData<Result<ApiResponse?>> =
        liveData {
            emit(Result.Loading)
            try {
                val response = apiService.postRegister(nama, email, pass, age, major, gender)
                _response.value = response

            } catch (e: Exception) {
                Log.d("Repository", "${e.message.toString()} ")
                emit(Result.Error(e.message.toString()))
            }
            val localData: LiveData<Result<ApiResponse?>> = response.map { Result.Success(it) }
            emitSource(localData)
        }

    companion object {

        @Volatile
        private var instance: Repository? = null
        fun getInstance(
            apiService: ApiService,
            appDatabase: AppDatabase
        ): Repository =
            instance ?: synchronized(this) {
                instance ?: Repository(apiService, appDatabase)
            }.also { instance = it }
    }
}