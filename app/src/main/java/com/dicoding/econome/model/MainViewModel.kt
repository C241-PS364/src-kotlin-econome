package com.dicoding.econome.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.econome.auth.AuthResponses
import com.dicoding.econome.response.Result
import com.dicoding.econome.util.Injection
import com.dicoding.econome.util.Repository

class MainViewModel(private val context: Context, private val repository: Repository) : ViewModel() {

    private val _loginResponse = MutableLiveData<Result<AuthResponses.LoginResponse>>()
    val loginResponse: LiveData<Result<AuthResponses.LoginResponse>> = _loginResponse

    private val _registerResponse = MutableLiveData<Result<AuthResponses.RegisterResponse>>()
    val registerResponse: LiveData<Result<AuthResponses.RegisterResponse>> = _registerResponse

    fun login(username: String, pass: String) {
        _loginResponse.value = Result.Loading
        repository.login(context, username, pass) { response, error ->
            if (error != null || response == null) {
                _loginResponse.postValue(Result.Error(error ?: "Unknown error"))
            } else {
                _loginResponse.postValue(Result.Success(response))
            }
        }
    }

    fun register(
        username:String,
        pass: String,
        name: String,
        gender: String,
        major: String,
        age: Int
    ) {
        _registerResponse.value = Result.Loading
        repository.register(username, pass, name, gender, major, age) { response, error ->
            if (error != null || response == null) {
                _registerResponse.postValue(Result.Error(error ?: "Unknown error"))
            } else {
                _registerResponse.postValue(Result.Success(response))
            }
        }
    }
}

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(context, Injection.provideRepo(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}
