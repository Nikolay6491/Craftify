package ru.netology.craftify.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.craftify.auth.AppAuth
import ru.netology.craftify.auth.AuthState
import ru.netology.craftify.auth.LoginFormState
import ru.netology.craftify.dto.MediaRequest
import ru.netology.craftify.model.PhotoModel
import ru.netology.craftify.repository.PostRepository
import java.io.File
import javax.inject.Inject

private val noPhotoAvatar = PhotoModel()

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val repository: PostRepository
): ViewModel() {
    val data: LiveData<AuthState> = appAuth
        .authStateFlow
        .asLiveData(Dispatchers.Default)

    private val _photoAvatar = MutableLiveData(noPhotoAvatar)
    val photoAvatar: LiveData<PhotoModel>
        get() = _photoAvatar

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    fun loginDataChanged(username: String, password: String) {
        _loginForm.value = LoginFormState(isDataValid = isUserNameValid(username) && isPasswordValid(password))
    }

    private fun isUserNameValid(username: String): Boolean {
        return username.isNotEmpty()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.isNotEmpty()
    }

    private fun isNameValid(password: String): Boolean {
        return password.isNotEmpty()
    }

    fun userAuthentication(login : String, password : String)  =  viewModelScope.launch {
        try {
            _loginForm.value = LoginFormState(isLoading = true)
            val account = repository.userAuthentication(login, password)
            appAuth.setAuth(account.id, account.token, account.name)
            _loginForm.value = LoginFormState(isDataValid = true)
        } catch (e: Exception) {
            _loginForm.value = LoginFormState(isError = true, isDataValid = true)
        }
    }

    fun userRegistration(login : String, password : String, name : String)  =  viewModelScope.launch {
        try {
            _loginForm.value = LoginFormState(isLoading = true)
            val account = when(_photoAvatar.value) {
                noPhotoAvatar -> repository.userRegistration(login, password, name)
                else -> _photoAvatar.value?.file?.let { file ->
                    repository.userRegistrationWithAvatar(login, password, name, MediaRequest(file))
                }
            }
            if (account != null) {
                appAuth.setAuth(account.id, account.token, account.name)
            }
            _loginForm.value = LoginFormState(isDataValid = true)
        } catch (e: Exception) {
            _loginForm.value = LoginFormState(isError = true, isDataValid = true)
        }
    }

    fun changeAvatar(uri: Uri?, file: File?) {
        _photoAvatar.value = PhotoModel(uri, file)
    }
}