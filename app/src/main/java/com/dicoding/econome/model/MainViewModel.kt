package com.dicoding.econome.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.econome.util.Injection
import com.dicoding.econome.util.Repository
import java.io.File

class MainViewModel(private val repository: Repository) : ViewModel() {
    private val mTempFile = MutableLiveData<File>()
    val tempFile: LiveData<File> = mTempFile

    private val mRotate = MutableLiveData<Boolean>().apply { postValue(true) }
    val rotate: LiveData<Boolean> = mRotate

    fun login(email: String, pass: String) = repository.login(email, pass)
    fun register(nama: String, email: String, pass: String, age: String, major: String, gender: String) =
        repository.register(nama, email, pass, age, major, gender)
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
