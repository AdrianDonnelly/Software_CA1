package com.example.carparts.data.remote

import com.example.carparts.util.getFirstNonBlank
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {

    private const val BASE_URL =
        "https://autopartsca-f2fecxe3g5e7dqhk.polandcentral-01.azurewebsites.net"

    internal fun parseErrorMessage(code: Int, rawError: String?): String {
        if (rawError.isNullOrBlank()) return "HTTP $code"

        return try {
            val root = Json.parseToJsonElement(rawError).jsonObject
            val title = root["title"]?.jsonPrimitive?.contentOrNull
            val errorsObj = root["errors"]?.jsonObject
            val validationMsg = errorsObj
                ?.entries
                ?.firstOrNull()
                ?.let { (field, value) ->
                    val first = value.jsonArray.firstOrNull()?.jsonPrimitive?.contentOrNull
                    if (first.isNullOrBlank()) null else "$field: $first"
                }

            listOfNotNull(title, validationMsg).firstOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: "HTTP $code"
        } catch (_: Exception) {
            rawError.take(300).ifBlank { "HTTP $code" }
        }
    }

    internal fun buildCheckoutUpdatePayload(
        part: Map<String, String>,
        quantity: Int
    ): Pair<String, String> {
        val partId = part.getFirstNonBlank("PartId", "partid", "id")
            ?: error("Missing PartId for checkout item.")
        val partIdInt = partId.toIntOrNull() ?: error("Invalid PartId: $partId")

        val currentStock = part.getFirstNonBlank("StockQuantity", "stockquantity", "stock", "quantity")
            ?.toIntOrNull() ?: 0
        val nextStock = (currentStock - quantity).coerceAtLeast(0)

        val body = buildJsonObject {
            put("partId", partIdInt)
            put("name", part.getFirstNonBlank("Name", "name") ?: "")
            put("partNumber", part.getFirstNonBlank("PartNumber", "partNumber", "part_number", "sku") ?: "")
            put("category", part.getFirstNonBlank("Category", "category") ?: "")
            put("manufacturer", part.getFirstNonBlank("Manufacturer", "manufacturer") ?: "")
            put("price", part.getFirstNonBlank("Price", "price")?.toDoubleOrNull() ?: 0.0)
            put("stockQuantity", nextStock)
            part.getFirstNonBlank("VehicleId", "vehicleId", "vehicleid")
                ?.toIntOrNull()
                ?.let { put("vehicleId", it) }
            part.getFirstNonBlank("Description", "description")
                ?.let { put("description", it) }
            part.getFirstNonBlank("ImageUrl", "imageUrl", "imageurl", "image_url")
                ?.let { put("imageUrl", it) }
            part.getFirstNonBlank("Condition", "condition")
                ?.let { put("condition", it) }
        }.toString()

        return partId to body
    }

    private suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        getInternal(path, withAuth = false)
    }

    private suspend fun getAuthenticated(path: String): String = withContext(Dispatchers.IO) {
        getInternal(path, withAuth = true)
    }

    private fun getInternal(path: String, withAuth: Boolean): String {
        val conn = URL("$BASE_URL$path").openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "application/json")
        if (withAuth) currentToken()?.let { conn.setRequestProperty("Authorization", "Bearer $it") }
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        val code = conn.responseCode
        if (code !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
            conn.disconnect()
            error(parseErrorMessage(code, err))
        }
        val body = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        return body
    }

    private fun currentToken(): String? =
        SupaBaseClient.client.auth.currentSessionOrNull()?.accessToken

    private suspend fun post(path: String, body: String) = withContext(Dispatchers.IO) {
        val conn = URL("$BASE_URL$path").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.setRequestProperty("Accept", "application/json")
        currentToken()?.let { conn.setRequestProperty("Authorization", "Bearer $it") }
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        conn.doOutput = true
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        if (code !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
            conn.disconnect()
            error(parseErrorMessage(code, err))
        }
        conn.disconnect()
    }

    private suspend fun put(path: String, body: String) = withContext(Dispatchers.IO) {
        val conn = URL("$BASE_URL$path").openConnection() as HttpURLConnection
        conn.requestMethod = "PUT"
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.setRequestProperty("Accept", "application/json")
        currentToken()?.let { conn.setRequestProperty("Authorization", "Bearer $it") }
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        conn.doOutput = true
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        if (code !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
            conn.disconnect()
            error(parseErrorMessage(code, err))
        }
        conn.disconnect()
    }

    private fun JsonObject.toFlatStringMap(): Map<String, String> =
        entries
            .filter { (_, v) -> v is JsonPrimitive }
            .associate { (k, v) -> k to ((v as JsonPrimitive).contentOrNull ?: "") }
            .filter { it.value.isNotBlank() }

    suspend fun fetchParts(): Result<List<Map<String, String>>> = try {
        val arr = Json.parseToJsonElement(get("/api/AutoParts")) as? JsonArray
            ?: JsonArray(emptyList())
        Result.success(arr.mapNotNull { (it as? JsonObject)?.toFlatStringMap() })
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun fetchVehicles(): Result<List<Map<String, String>>> = try {
        val arr = Json.parseToJsonElement(get("/api/Vehicles")) as? JsonArray
            ?: JsonArray(emptyList())
        Result.success(arr.mapNotNull { (it as? JsonObject)?.toFlatStringMap() })
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun insertPart(partData: Map<String, String>): Result<Unit> = try {
        val body = buildJsonObject {
            partData["Name"]?.takeIf { it.isNotBlank() }?.let { put("name", it) }
            partData["PartNumber"]?.takeIf { it.isNotBlank() }?.let { put("partNumber", it) }
            partData["Category"]?.takeIf { it.isNotBlank() }?.let { put("category", it) }
            partData["Manufacturer"]?.takeIf { it.isNotBlank() }?.let { put("manufacturer", it) }
            put("price", partData["Price"]?.toDoubleOrNull() ?: 0.0)
            put("stockQuantity", partData["StockQuantity"]?.toIntOrNull() ?: 0)
            partData["VehicleId"]?.toIntOrNull()?.let { put("vehicleId", it) }
            partData["Description"]?.takeIf { it.isNotBlank() }?.let { put("description", it) }
            partData["ImageUrl"]?.takeIf { it.isNotBlank() }?.let { put("imageUrl", it) }
            partData["Condition"]?.takeIf { it.isNotBlank() }?.let { put("condition", it) }
        }.toString()
        post("/api/AutoParts", body)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun insertVehicle(vehicleData: Map<String, String>): Result<Unit> = try {
        val body = buildJsonObject {
            vehicleData["Make"]?.takeIf { it.isNotBlank() }?.let { put("make", it) }
            vehicleData["Model"]?.takeIf { it.isNotBlank() }?.let { put("model", it) }
            vehicleData["Year"]?.toIntOrNull()?.let { put("year", it) }
            vehicleData["EngineType"]?.takeIf { it.isNotBlank() }?.let { put("engineType", it) }
            vehicleData["Category"]?.takeIf { it.isNotBlank() }?.let { put("category", it) }
            vehicleData["ImageUrl"]?.takeIf { it.isNotBlank() }?.let { put("imageUrl", it) }
        }.toString()
        post("/api/Vehicles", body)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun checkoutParts(items: List<Pair<Map<String, String>, Int>>): Result<Unit> = try {
        items.forEach { (part, quantity) ->
            if (quantity <= 0) return@forEach
            val (partId, body) = buildCheckoutUpdatePayload(part, quantity)
            put("/api/AutoParts/$partId", body)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
