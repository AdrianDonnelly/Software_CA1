package com.example.carparts.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {

    private const val BASE_URL =
        "https://autopartsca-f2fecxe3g5e7dqhk.polandcentral-01.azurewebsites.net"

    private suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        val conn = URL("$BASE_URL$path").openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "application/json")
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        val code = conn.responseCode
        if (code !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
            conn.disconnect()
            error(err)
        }
        val body = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        body
    }

    private suspend fun post(path: String, body: String) = withContext(Dispatchers.IO) {
        val conn = URL("$BASE_URL$path").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.setRequestProperty("Accept", "application/json")
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        conn.doOutput = true
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        if (code !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
            conn.disconnect()
            error(err)
        }
        conn.disconnect()
    }

    private suspend fun put(path: String, body: String) = withContext(Dispatchers.IO) {
        val conn = URL("$BASE_URL$path").openConnection() as HttpURLConnection
        conn.requestMethod = "PUT"
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.setRequestProperty("Accept", "application/json")
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        conn.doOutput = true
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        if (code !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
            conn.disconnect()
            error(err)
        }
        conn.disconnect()
    }

    // Flatten a JSON object to Map<String, String>, skipping nested objects/arrays and nulls.
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
            val partId = part.entries.firstOrNull { it.key.equals("PartId", ignoreCase = true) }?.value
                ?: part.entries.firstOrNull { it.key.equals("id", ignoreCase = true) }?.value
                ?: error("Missing PartId for checkout item.")
            val currentStock = part.entries.firstOrNull {
                it.key.equals("StockQuantity", ignoreCase = true) ||
                    it.key.equals("stockquantity", ignoreCase = true)
            }?.value?.toIntOrNull() ?: 0
            val nextStock = (currentStock - quantity).coerceAtLeast(0)

            val body = buildJsonObject {
                put("partId", partId.toIntOrNull() ?: error("Invalid PartId: $partId"))
                put("stockQuantity", nextStock)
            }.toString()

            put("/api/AutoParts/$partId", body)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
