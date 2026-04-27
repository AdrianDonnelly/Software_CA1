package com.example.carparts.data.remote

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

object AuthRepository {

    private fun Throwable.readableAuthError(defaultMessage: String): String {
        val msg = message?.trim().orEmpty()
        val lower = msg.lowercase()
        if (msg.contains("404", ignoreCase = true) ||
            msg.contains("Not Found", ignoreCase = true)
        ) {
            return "Auth endpoint not found. Check SUPABASE_URL in local.properties."
        }
        if (lower.contains("unable to resolve host") || lower.contains("failed to connect")) {
            return "Network error. Check emulator internet and try again."
        }
        if (msg.contains("401", ignoreCase = true) || lower.contains("invalid api key")) {
            return "Auth key/config error. Check SUPABASE_ANON_KEY in local.properties."
        }
        if (msg.isBlank()) return defaultMessage
        return msg
    }

    suspend fun signUp(email: String, password: String): Result<Unit> {
        return runCatching {
            SupaBaseClient.client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
        }.map { Unit }
            .mapError { it.readableAuthError("Sign up failed.") }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return runCatching {
            SupaBaseClient.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }.map { Unit }
            .mapError { it.readableAuthError("Sign in failed.") }
    }

    private inline fun <T> Result<T>.mapError(mapper: (Throwable) -> String): Result<T> {
        return fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(IllegalStateException(mapper(it), it)) }
        )
    }

    suspend fun signOut(): Result<Unit> {
        return runCatching {
            SupaBaseClient.client.auth.signOut()
        }
    }

    fun getCurrentUserEmail(): String? {
        return SupaBaseClient.client.auth.currentUserOrNull()?.email
    }

    fun isAdmin(): Boolean {
        val user = SupaBaseClient.client.auth.currentUserOrNull() ?: return false
        val role = user.appMetadata?.get("role")?.jsonPrimitive?.contentOrNull
        return role == "admin"
    }

    fun getCurrentUserProfile(): UserProfile? {
        val user = SupaBaseClient.client.auth.currentUserOrNull() ?: return null
        return UserProfile(
            email = user.email,
            id = user.id,
            createdAt = user.createdAt.toString(),
            lastSignInAt = user.lastSignInAt?.toString()
        )
    }
}

data class UserProfile(
    val email: String?,
    val id: String,
    val createdAt: String,
    val lastSignInAt: String?
)