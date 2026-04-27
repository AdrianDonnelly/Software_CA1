package com.example.carparts.data.remote

import com.example.carparts.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient

object SupaBaseClient {

    private val supabaseUrl = BuildConfig.SUPABASE_URL
    private val supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseAnonKey
        ) {
            install(plugin = Auth)
        }
    }
}
