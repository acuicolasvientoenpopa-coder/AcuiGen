package com.nfctags.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userEmail: String? = null,
    val userId: String? = null
)

sealed class AuthEvent {
    data class Success(val message: String) : AuthEvent()
    data class Error(val message: String) : AuthEvent()
    data object LoggedOut : AuthEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isLoggedIn = tokenManager.isLoggedIn,
            userEmail = tokenManager.userEmail,
            userId = tokenManager.userId
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        isLoggedIn = true,
                        userEmail = tokenManager.userEmail,
                        userId = tokenManager.userId
                    )
                    _events.emit(AuthEvent.Success("Sesión iniciada correctamente"))
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(AuthEvent.Error(result.message))
                }
            }
        }
    }

    fun signup(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = false)
            when (val result = authRepository.signup(email, password)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        isLoggedIn = true,
                        userEmail = tokenManager.userEmail,
                        userId = tokenManager.userId
                    )
                    _events.emit(AuthEvent.Success("Cuenta creada correctamente"))
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(AuthEvent.Error(result.message))
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _uiState.value = AuthUiState()
                _events.emit(AuthEvent.LoggedOut)
                _events.emit(AuthEvent.Success("Sesión cerrada"))
            } catch (e: Exception) {
                _events.emit(AuthEvent.Error("Error al cerrar sesión: ${e.message}"))
            }
        }
    }

    fun recoverPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = authRepository.recoverPassword(email)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(AuthEvent.Success("Correo de recuperación enviado. Revisa tu bandeja de entrada"))
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(AuthEvent.Error(result.message))
                }
            }
        }
    }

    fun checkSession() {
        viewModelScope.launch {
            if (tokenManager.isLoggedIn && tokenManager.expiresAt < System.currentTimeMillis() + 300000) {
                authRepository.refreshToken()
            }
            _uiState.value = _uiState.value.copy(
                isLoggedIn = tokenManager.isLoggedIn,
                userEmail = tokenManager.userEmail,
                userId = tokenManager.userId
            )
        }
    }
}
