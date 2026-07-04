package com.nfctags.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class SignupDirectRequest(
    val email: String,
    val password: String,
    val nombre: String = "Usuario"
)

data class SignupDirectResponse(
    val user: SignupUser?,
    val message: String?
)

data class SignupUser(
    val id: String,
    val email: String?,
    val nombre: String?
)

interface BackendApiService {

    @POST("auth/signup-direct")
    suspend fun signupDirect(@Body request: SignupDirectRequest): Response<SignupDirectResponse>
}
