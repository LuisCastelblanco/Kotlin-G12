package com.example.explorandes.repositories

import com.example.explorandes.api.ApiClient
import com.example.explorandes.models.AuthRequest
import com.example.explorandes.models.AuthResponse
import com.example.explorandes.models.RegisterRequest
import com.example.explorandes.models.User
import com.example.explorandes.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AuthRepository(private val sessionManager: SessionManager) {
    private val apiService = ApiClient.apiService

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val authRequest = AuthRequest(email = email, password = password)
                val response = apiService.login(authRequest)

                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        // Guardar el token en SessionManager
                        sessionManager.saveToken(authResponse.token)
                        Result.success(authResponse)
                    } ?: Result.failure(Exception("Respuesta vacía del servidor"))
                } else {
                    Result.failure(Exception("Error de login: ${response.code()} - ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun register(username: String, email: String, password: String, firstName: String? = null, lastName: String? = null): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Usar RegisterRequest en lugar de User
                val registerRequest = RegisterRequest(
                    username = username,
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName
                )

                val response = apiService.register(registerRequest)

                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        // Guardar el token en SessionManager
                        sessionManager.saveToken(authResponse.token)
                        Result.success(authResponse)
                    } ?: Result.failure(Exception("Respuesta vacía del servidor"))
                } else {
                    Result.failure(Exception("Error de registro: ${response.code()} - ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}