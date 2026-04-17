package com.example.carparts.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient

object SupaBaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://hjowdpfrbgdfstmwcymu.supabase.co",
        supabaseKey = "sb_publishable_kr4kqLncxHaIyeaflS8XkA_tsGrTVXS"
    ) {
        install(Auth)
    }
}