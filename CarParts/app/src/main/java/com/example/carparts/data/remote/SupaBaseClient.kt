package com.example.carparts.data.remote

import android.util.Log
import com.example.carparts.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

object SupaBaseClient {

    private val supabaseUrl = BuildConfig.SUPABASE_URL
    private val supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseAnonKey
        ) {
            install(plugin = Auth)
            install(plugin = Postgrest)
        }
    }

    suspend fun testVehicles() {
        try {
            if (supabaseUrl.isBlank() || supabaseAnonKey.isBlank()) {
                Log.e("SUPABASE", "Missing SUPABASE_URL or SUPABASE_ANON_KEY in local.properties")
                return
            }
            val result = client
                .from("Vehicles")
                .select()

            Log.d("SUPABASE", "SUCCESS: $result")
        } catch (e: Exception) {
            Log.e("SUPABASE", "ERROR: ${e.message}", e)
        }
    }

    suspend fun insertPart(partData: Map<String, String>): Result<Unit> {
        return try {
            val json = buildJsonObject {
                partData.forEach { (k, v) -> if (v.isNotBlank()) put(k, v) }
            }
            client.from("AutoParts").insert(json)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchParts(): Result<List<Map<String, String>>> {
        return try {
            if (supabaseUrl.isBlank() || supabaseAnonKey.isBlank()) {
                return Result.failure(
                    IllegalStateException(
                        "Missing Supabase config. Add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties."
                    )
                )
            }

            val responseData = client
                .from("AutoParts")
                .select()
                .data

            val jsonArray = Json.parseToJsonElement(responseData) as? JsonArray
                ?: JsonArray(emptyList())

            val safeRows = jsonArray.mapNotNull { item ->
                val row = item as? JsonObject ?: return@mapNotNull null
                row.mapValues { (_, value) ->
                    when (value) {
                        is JsonPrimitive -> value.contentOrNull ?: value.toString()
                        else -> value.toString()
                    }
                }
            }

            Result.success(safeRows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}