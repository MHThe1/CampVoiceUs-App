package com.work.campvoiceus.viewmodels

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.models.EditProfileData
import com.work.campvoiceus.network.RetrofitInstance.userService
import com.work.campvoiceus.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileEditViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _profileData = MutableStateFlow(EditProfileData())
    val profileData: StateFlow<EditProfileData> = _profileData

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchProfile() // Automatically fetch the profile on initialization
    }

    fun fetchProfile() {
        _loading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                val response = userService.getUserProfile("Bearer $token")
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        _profileData.value = EditProfileData(
                            name = user.name.orEmpty(),
                            bio = user.bio.orEmpty(),
                            avatarUrl = user.avatarUrl
                        )
                    } else {
                        _errorMessage.value = "No profile data found."
                    }
                } else {
                    _errorMessage.value = "Failed to fetch profile: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching profile: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }




    fun updateProfile(updatedData: EditProfileData, avatarUri: Uri?, contentResolver: ContentResolver) {
        _loading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()

                // Prepare name and bio as part of the request body
                val profileData = mapOf(
                    "name" to updatedData.name.toRequestBody("text/plain".toMediaTypeOrNull()),
                    "bio" to (updatedData.bio ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
                )

                // Prepare the image as a MultipartBody.Part if provided
                val avatarPart = avatarUri?.let {
                    val inputStream = contentResolver.openInputStream(it)
                    val tempFile = File.createTempFile("avatar", null)
                    inputStream?.use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    MultipartBody.Part.createFormData(
                        "avatarUrl", // Backend expects this key
                        tempFile.name,
                        tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                }

                // Make the API request
                val response = userService.updateUserProfile(
                    token = "Bearer $token",
                    profileData = profileData,
                    avatar = avatarPart
                )

                if (response.isSuccessful) {
                    fetchProfile() // Refresh profile data after update
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
