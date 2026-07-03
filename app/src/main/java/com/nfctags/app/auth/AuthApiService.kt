package com.nfctags.app.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class AuthLoginRequest(
    val email: String,
    val password: String
)

data class AuthSignupRequest(
    val email: String,
    val password: String
)

data class AuthRefreshRequest(
    val refresh_token: String
)

data class AuthRecoverRequest(
    val email: String
)

data class AuthSessionResponse(
    val access_token: String?,
    val token_type: String?,
    val expires_in: Long?,
    val refresh_token: String?,
    val user: AuthUser?
)

data class AuthUser(
    val id: String,
    val email: String?,
    val created_at: String?
)

data class AuthError(
    val error: String?,
    val error_description: String?,
    val message: String?
)

interface AuthApiService {

    @POST("token?grant_type=password")
    suspend fun login(@Body request: AuthLoginRequest): Response<AuthSessionResponse>

    @POST("token?grant_type=refresh_token")
    suspend fun refreshToken(@Body request: AuthRefreshRequest): Response<AuthSessionResponse>

    @POST("signup")
    suspend fun signup(@Body request: AuthSignupRequest): Response<AuthSessionResponse>

    @POST("logout")
    suspend fun logout(): Response<Unit>

    @POST("recover")
    suspend fun recoverPassword(@Body request: AuthRecoverRequest): Response<Unit>

    @GET("user")
    suspend fun getUser(): Response<AuthUser>
}
