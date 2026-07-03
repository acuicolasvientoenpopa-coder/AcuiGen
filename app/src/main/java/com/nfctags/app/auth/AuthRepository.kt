package com.nfctags.app.auth

import android.util.Log
import com.google.gson.Gson
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data object Success : AuthResult()
    data object EmailConfirmationRequired : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApiService,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "AuthRepository"
    }

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response = authApi.login(AuthLoginRequest(email, password))
            handleSessionResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            AuthResult.Error("Error de conexión: ${e.localizedMessage ?: "Sin conexión"}")
        }
    }

    suspend fun signup(email: String, password: String): AuthResult {
        return try {
            val response = authApi.signup(AuthSignupRequest(email, password))
            handleSessionResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "Signup error", e)
            AuthResult.Error("Error de conexión: ${e.localizedMessage ?: "Sin conexión"}")
        }
    }

    suspend fun refreshToken(): Boolean {
        val currentRefresh = tokenManager.refreshToken ?: return false
        return try {
            val response = authApi.refreshToken(AuthRefreshRequest(currentRefresh))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    tokenManager.saveSession(
                        accessToken = body.access_token,
                        refreshToken = body.refresh_token,
                        userId = body.user?.id ?: "",
                        email = body.user?.email,
                        expiresIn = body.expires_in
                    )
                    true
                } else false
            } else {
                tokenManager.clearSession()
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Refresh error", e)
            false
        }
    }

    suspend fun logout() {
        try {
            authApi.logout()
        } catch (e: Exception) {
            Log.e(TAG, "Logout API error", e)
        } finally {
            tokenManager.clearSession()
        }
    }

    suspend fun recoverPassword(email: String): AuthResult {
        return try {
            val response = authApi.recoverPassword(AuthRecoverRequest(email))
            if (response.isSuccessful) {
                AuthResult.Success
            } else {
                val errorBody = response.errorBody()?.string()
                val msg = try {
                    Gson().fromJson(errorBody, AuthError::class.java)?.let {
                        it.error_description ?: it.message ?: it.error ?: "Error al enviar"
                    } ?: "Error ${response.code()}"
                } catch (_: Exception) {
                    "Error ${response.code()}"
                }
                AuthResult.Error(msg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Recover password error", e)
            AuthResult.Error("Error de conexión: ${e.localizedMessage ?: "Sin conexión"}")
        }
    }

    private fun handleSessionResponse(response: Response<AuthSessionResponse>): AuthResult {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.user != null) {
                if (body.access_token.isBlank()) {
                    AuthResult.EmailConfirmationRequired
                } else {
                    tokenManager.saveSession(
                        accessToken = body.access_token,
                        refreshToken = body.refresh_token,
                        userId = body.user.id,
                        email = body.user.email,
                        expiresIn = body.expires_in
                    )
                    AuthResult.Success
                }
            } else {
                val errorBody = response.errorBody()?.string()
                AuthResult.Error(errorBody ?: "Error desconocido")
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val msg = try {
                Gson().fromJson(errorBody, AuthError::class.java)?.let {
                    it.error_description ?: it.message ?: it.error ?: "Error desconocido"
                } ?: "Error ${response.code()}"
            } catch (_: Exception) {
                "Error ${response.code()}: ${response.message()}"
            }
            AuthResult.Error(msg)
        }
    }
}
