package com.example.carparts.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiClientCheckoutTest {

    @Test
    fun buildCheckoutUpdatePayload_includesRequiredFields_andDeductsStock() {
        val part = mapOf(
            "PartId" to "12",
            "Name" to "Brake Pad",
            "PartNumber" to "BP-001",
            "Category" to "Brakes",
            "Manufacturer" to "OEM",
            "Price" to "49.99",
            "StockQuantity" to "8",
            "VehicleId" to "3",
            "Description" to "Front brake pad",
            "ImageUrl" to "https://example.com/brake.jpg",
            "Condition" to "New"
        )

        val (partId, payload) = ApiClient.buildCheckoutUpdatePayload(part, quantity = 3)
        val json = Json.parseToJsonElement(payload).jsonObject

        assertEquals("12", partId)
        assertEquals("12", json["partId"]?.jsonPrimitive?.content)
        assertEquals("Brake Pad", json["name"]?.jsonPrimitive?.content)
        assertEquals("BP-001", json["partNumber"]?.jsonPrimitive?.content)
        assertEquals("Brakes", json["category"]?.jsonPrimitive?.content)
        assertEquals("OEM", json["manufacturer"]?.jsonPrimitive?.content)
        assertEquals("49.99", json["price"]?.jsonPrimitive?.content)
        assertEquals("5", json["stockQuantity"]?.jsonPrimitive?.content)
        assertEquals("3", json["vehicleId"]?.jsonPrimitive?.content)
    }

    @Test
    fun buildCheckoutUpdatePayload_clampsStockToZero_whenQuantityExceedsStock() {
        val part = mapOf(
            "PartId" to "7",
            "Name" to "Oil Filter",
            "PartNumber" to "OF-100",
            "Category" to "Engine",
            "StockQuantity" to "1"
        )

        val (_, payload) = ApiClient.buildCheckoutUpdatePayload(part, quantity = 4)
        val json = Json.parseToJsonElement(payload).jsonObject

        assertEquals("0", json["stockQuantity"]?.jsonPrimitive?.content)
    }

    @Test
    fun parseErrorMessage_extractsValidationMessage() {
        val raw = """{"title":"One or more validation errors occurred.","errors":{"Name":["The Name field is required."]}}"""
        val msg = ApiClient.parseErrorMessage(400, raw)
        assertTrue(msg.contains("validation errors", ignoreCase = true))
    }

    @Test
    fun parseErrorMessage_fallsBackToHttpCode_whenBodyBlank() {
        val msg = ApiClient.parseErrorMessage(500, "")
        assertEquals("HTTP 500", msg)
    }
}
