package com.example.carparts.data.remote

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

object AuthRepository {

    suspend fun signUp(email: String, password: String): Result<Unit> {
        return runCatching {
            SupaBaseClient.client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return runCatching {
            SupaBaseClient.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    suspend fun signOut(): Result<Unit> {
        return runCatching {
            SupaBaseClient.client.auth.signOut()
        }
    }

    fun getCurrentUserEmail(): String? {
        return SupaBaseClient.client.auth.currentUserOrNull()?.email
    }
}