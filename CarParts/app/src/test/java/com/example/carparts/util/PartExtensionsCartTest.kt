package com.example.carparts.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PartExtensionsCartTest {

    @Test
    fun basketKey_returnsPartId_whenPresent() {
        val part = mapOf("PartId" to "42")

        val key = part.basketKey()

        assertEquals("42", key)
    }

    @Test
    fun basketKey_returnsFallback_whenMissing() {
        val key = emptyMap<String, String>().basketKey()

        assertEquals("unknown-part", key)
    }

    @Test
    fun readStockQuantity_clampsNegativeStock_toZero() {
        val part = mapOf("StockQuantity" to "-5")

        val stock = part.readStockQuantity()

        assertEquals(0, stock)
    }

    @Test
    fun toPriceLabel_formatsValidPrice_withEuroAndTwoDecimals() {
        val label = "49.9".toPriceLabel()

        assertEquals("€49.90", label)
    }

    @Test
    fun toPriceLabel_returnsDash_whenValueInvalid() {
        val label = "not-a-number".toPriceLabel()

        assertEquals("-", label)
    }
}
