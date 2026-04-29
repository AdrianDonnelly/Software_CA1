package com.example.carparts

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.carparts.ui.PartDetailsDialog
import com.example.carparts.ui.PartRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PartsUiTesting {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun partRow_cardClick_invokesOnClickCallback() {
        var partClicked = false

        composeRule.setContent {
            PartRow(
                part = mapOf(
                    "Name" to "Brake Disc",
                    "Price" to "59.99",
                    "StockQuantity" to "7",
                    "PartNumber" to "BD-100"
                ),
                onClick = { partClicked = true },
                onAddToBasket = {},
                canAddToBasket = true
            )
        }

        composeRule.onNodeWithText("Brake Disc").performClick()
        assertTrue(partClicked)
    }

    @Test
    fun partRow_outOfStock_showsDisabledButton() {
        val outOfStockLabel = targetContext.getString(R.string.btn_out_of_stock)

        composeRule.setContent {
            PartRow(
                part = mapOf(
                    "Name" to "Oil Pump",
                    "Price" to "99.99",
                    "StockQuantity" to "0"
                ),
                onClick = {},
                onAddToBasket = {},
                canAddToBasket = false
            )
        }

        composeRule.onNodeWithText(outOfStockLabel).assertIsNotEnabled()
    }

    @Test
    fun partRow_emptyPart_showsEmptyRowMessage() {
        val emptyRowLabel = targetContext.getString(R.string.empty_row)

        composeRule.setContent {
            PartRow(
                part = emptyMap(),
                onClick = {},
                onAddToBasket = {},
                canAddToBasket = false
            )
        }

        composeRule.onNodeWithText(emptyRowLabel).assertIsDisplayed()
    }

    @Test
    fun partDetailsDialog_closeButton_invokesDismissCallback() {
        val closeLabel = targetContext.getString(R.string.btn_close)
        var dismissCalls = 0

        composeRule.setContent {
            PartDetailsDialog(
                part = mapOf(
                    "PartId" to "22",
                    "Name" to "Air Filter",
                    "Price" to "19.99",
                    "StockQuantity" to "3"
                ),
                onDismiss = { dismissCalls++ },
                onAddToBasket = {},
                canAddToBasket = true
            )
        }

        composeRule.onNodeWithText(closeLabel).performClick()
        assertEquals(1, dismissCalls)
    }
}
