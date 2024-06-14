package com.dicoding.econome.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.econome.auth.ProfileData
import com.dicoding.econome.auth.UpdateProfileRequest

import com.dicoding.econome.util.SettingPreference
import kotlinx.coroutines.launch

class SettingViewModel(private val pref: SettingPreference) : ViewModel() {

    fun getThemeSettings(): LiveData<ProfileData> {
        return pref.getUserData().asLiveData()
    }

    fun saveThemeSetting(userData: UpdateProfileRequest) {
        viewModelScope.launch {
            pref.putUserData(userData)
        }
    }

    fun deleteData() {
        viewModelScope.launch {
            pref.clearUserData()
        }
    }
}

class SettingFactory(private val pref: SettingPreference) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            return SettingViewModel(pref) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}