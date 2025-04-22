package com.example.explorandes.repositories

import com.example.explorandes.api.ApiClient
import com.example.explorandes.models.User

class UserRepository {

    suspend fun getUserById(id: Long): User? {
        val response = ApiClient.apiService.getUserById(id)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun updateUser(id: Long, user: User): User? {
        val response = ApiClient.apiService.updateUser(id, user)
        return if (response.isSuccessful) response.body() else null
    }
}