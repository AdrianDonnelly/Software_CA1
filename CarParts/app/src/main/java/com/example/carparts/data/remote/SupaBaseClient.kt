package com.example.carparts.data.remote

import android.util.Log
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from

object SupaBaseClient {

    val client = createSupabaseClient(
        supabaseUrl = "https://hjowdpfrbgdfstmwcymu.supabase.co",
        supabaseKey = "sb_publishable_kr4kqLncxHaIyeaflS8XkA_tsGrTVXS"
    ) {
        install(plugin = Auth)
        install(plugin = Postgrest)
    }

    suspend fun testVehicles() {
        try {
            val result = client
                .from("Vehicles")
                .select()

            Log.d("SUPABASE", "SUCCESS: $result")
        } catch (e: Exception) {
            Log.e("SUPABASE", "ERROR: ${e.message}", e)
        }
    }
}