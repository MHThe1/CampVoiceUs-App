package com.work.campvoiceus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.models.EditProfileData
import com.work.campvoiceus.network.RetrofitInstance.userService
import com.work.campvoiceus.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileEditViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _profileData = MutableStateFlow(EditProfileData())
    val profileData: StateFlow<EditProfileData> = _profileData

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchProfile() {
        _loading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                val response = userService.getUserProfile("Bearer $token")
                if (response.isSuccessful) {
                    val user = response.body()
                    _profileData.value = EditProfileData(
                        name = user?.name.orEmpty(),
                        bio = user?.bio.orEmpty(),
                        avatarUrl = user?.avatarUrl.orEmpty()
                    )
                } else {
                    _errorMessage.value = "Failed to fetch profile: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateProfile(updatedData: EditProfileData) {
        _loading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                val response = userService.updateUserProfile(
                    "Bearer $token",
                    mapOf(
                        "name" to updatedData.name,
                        "bio" to updatedData.bio.orEmpty()
                    )
                )
                if (response.isSuccessful) {
                    fetchProfile() // Refresh profile data
                } else {
                    _errorMessage.value = "Failed to update profile: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating profile: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateName(newName: String) {
        _profileData.value = _profileData.value.copy(name = newName)
    }

    fun updateBio(newBio: String) {
        _profileData.value = _profileData.value.copy(bio = newBio)
    }
}
