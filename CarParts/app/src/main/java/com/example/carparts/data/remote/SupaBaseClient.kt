package com.example.carparts.data.remote

import com.example.carparts.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient

object SupaBaseClient {

    private fun normalizeSupabaseUrl(raw: String): String {
        val trimmed = raw.trim().removeSuffix("/")
        val withoutApiPath = trimmed
            .removeSuffix("/auth/v1")
            .removeSuffix("/rest/v1")
        return if (withoutApiPath.startsWith("http://") || withoutApiPath.startsWith("https://")) {
            withoutApiPath
        } else {
            "https://$withoutApiPath"
        }
    }

    private val supabaseUrl = normalizeSupabaseUrl(BuildConfig.SUPABASE_URL)
    private val supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY

    val client by lazy {
        check(supabaseUrl.isNotBlank() && supabaseUrl != "https://") {
            "Missing SUPABASE_URL in local.properties"
        }
        check(supabaseAnonKey.isNotBlank()) {
            "Missing SUPABASE_ANON_KEY in local.properties"
        }
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseAnonKey
        ) {
            install(plugin = Auth)
        }
    }

    suspend fun close() {
        runCatching { client.close() }
    }
}
