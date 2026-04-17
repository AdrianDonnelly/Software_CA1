package com.example.carparts.data.remote

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

object AuthRepository {

    suspend fun signUp(email: String, password: String) {
        SupaBaseClient.client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }
}