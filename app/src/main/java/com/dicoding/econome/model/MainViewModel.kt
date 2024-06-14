package com.dicoding.econome.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.econome.auth.RegisterResponse
import com.dicoding.econome.response.LoginResponse
import com.dicoding.econome.util.Injection
import com.dicoding.econome.util.Repository
import java.io.File

class MainViewModel(private val repository: Repository) : ViewModel() {
    private val mTempFile = MutableLiveData<File>()
    val tempFile: LiveData<File> = mTempFile

    private val mRotate = MutableLiveData<Boolean>().apply { postValue(true) }
    val rotate: LiveData<Boolean> = mRotate

    private val _loginResponse = MutableLiveData<LoginResponse?>()
    val loginResponse: MutableLiveData<LoginResponse?> = _loginResponse

    private val _registerResponse = MutableLiveData<RegisterResponse?>()
    val registerResponse: MutableLiveData<RegisterResponse?> = _registerResponse

    fun login(email: String, pass: String) {
        repository.login(email, pass) { response ->
            _loginResponse.postValue(response)
        }
    }

    fun register(
        name: String,
        email: String,
        pass: String,
        age: String,
        major: String,
        gender: String
    ) {
        repository.register(name, email, pass, age, major, gender) { response ->
            _registerResponse.postValue(response)
        }
    }
}

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(Injection.provideRepo(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}