package com.firman.capstone.urvoice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firman.capstone.urvoice.data.local.datastore.AuthPreferences
import com.firman.capstone.urvoice.data.remote.models.CurrentUserResponse
import com.firman.capstone.urvoice.data.remote.models.DeleteUserResponse
import com.firman.capstone.urvoice.data.remote.models.UserLogoutResponse
import com.firman.capstone.urvoice.data.remote.models.UserProfileResponse
import com.firman.capstone.urvoice.data.remote.models.UserResponse
import com.firman.capstone.urvoice.data.repository.user.UserRepository
import com.firman.capstone.urvoice.utils.GalleryUtils
import com.firman.capstone.urvoice.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _currentUserProfile =
        MutableStateFlow<ResultState<CurrentUserResponse>>(ResultState.Initial)
    val currentUserProfile: StateFlow<ResultState<CurrentUserResponse>> =
        _currentUserProfile.asStateFlow()

    private val _updateUserState = MutableStateFlow<ResultState<UserResponse>>(ResultState.Initial)
    val updateUserState: StateFlow<ResultState<UserResponse>> = _updateUserState.asStateFlow()

    private val _deleteUser = MutableStateFlow<ResultState<DeleteUserResponse>>(ResultState.Initial)
    val deleteUserState: StateFlow<ResultState<DeleteUserResponse>> = _deleteUser.asStateFlow()

    private val _logoutUser = MutableStateFlow<ResultState<UserLogoutResponse>>(ResultState.Initial)
    val logoutUserState: StateFlow<ResultState<UserLogoutResponse>> = _logoutUser.asStateFlow()

    // NEW: Upload Profile Image State
    private val _uploadProfileImageState = MutableStateFlow<ResultState<UserProfileResponse>>(ResultState.Initial)
    val uploadProfileImageState: StateFlow<ResultState<UserProfileResponse>> = _uploadProfileImageState.asStateFlow()

    init {
        getCurrentUser()
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            _currentUserProfile.value = ResultState.Loading
            try {
                userRepository.getCurrentUser().collect { result ->
                    _currentUserProfile.value = result
                }
            } catch (e: Exception) {
                _currentUserProfile.value =
                    ResultState.Error("Failed to load user data: ${e.message}")
            }
        }
    }

    fun updateUserProfile(name: String, password: String?) {
        viewModelScope.launch {
            _updateUserState.value = ResultState.Loading
            try {
                userRepository.updateUser(name, password ?: "")
                    .collect { result ->
                        _updateUserState.value = result
                        if (result is ResultState.Success) {
                            getCurrentUser() // Refresh user data after update
                        }
                    }
            } catch (e: Exception) {
                _updateUserState.value = ResultState.Error("Update failed: ${e.message}")
            }
        }
    }

    fun uploadProfileImage(imageFile: File) {
        viewModelScope.launch {
            _uploadProfileImageState.value = ResultState.Loading
            try {
                val multipartBody = GalleryUtils.createImageMultipart(
                    file = imageFile,
                    partName = "profileImage"
                )

                userRepository.updateUserProfile(multipartBody).collect { result ->
                    _uploadProfileImageState.value = result
                    if (result is ResultState.Success) {
                        getCurrentUser()
                    }
                }
            } catch (e: Exception) {
                _uploadProfileImageState.value = ResultState.Error("Upload failed: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutUser.value = ResultState.Loading
            try {
                userRepository.logout().collect { result ->
                    _logoutUser.value = result
                    if (result is ResultState.Success) {
                        authPreferences.clearSession()
                    }
                }
            } catch (e: Exception) {
                _logoutUser.value = ResultState.Error("Logout failed: ${e.message}")
            }
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            _deleteUser.value = ResultState.Loading
            try {
                userRepository.deleteUser().collect { result ->
                    _deleteUser.value = result
                    if (result is ResultState.Success) {
                        authPreferences.clearSession()
                    }
                }
            } catch (e: Exception) {
                _deleteUser.value = ResultState.Error("Failed to delete user: ${e.message}")
            }
        }
    }

    fun resetUploadState() {
        _uploadProfileImageState.value = ResultState.Initial
    }

}