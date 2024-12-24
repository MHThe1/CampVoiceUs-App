package com.work.campvoiceus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.network.UserService
import com.work.campvoiceus.utils.TokenManager
import kotlinx.coroutines.launch

data class Voter(
    val username: String,
    val name: String,
    val avatarUrl: String
)

class VoterListViewModel(
    private val tokenManager: TokenManager,
    private val userService: UserService
) : ViewModel() {

    fun fetchVoters(
        voterIds: List<String>,
        onResult: (List<Voter>) -> Unit
    ) {
        viewModelScope.launch {
            val token = tokenManager.getToken() // Fetch token
            val fetchedVoters = mutableListOf<Voter>()

            try {
                for (id in voterIds) {
                    val response = userService.getUserById(
                        token = "Bearer $token",
                        idMap = mapOf("id" to id)
                    )
                    if (response.isSuccessful) {
                        response.body()?.let { user ->
                            fetchedVoters.add(
                                Voter(
                                    username = user.username ?: "unknown",
                                    name = user.name ?: "Unknown User",
                                    avatarUrl = user.avatarUrl ?: ""
                                )
                            )
                        }
                    }
                }
                onResult(fetchedVoters) // Return the fetched voter list
            } catch (e: Exception) {
                onResult(emptyList()) // Return an empty list in case of failure
            }
        }
    }
}

