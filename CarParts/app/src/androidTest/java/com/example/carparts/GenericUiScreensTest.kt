package com.example.carparts

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.carparts.ui.CartScreen
import com.example.carparts.ui.ProfileScreen
import com.example.carparts.ui.VehiclePickerDialog
import com.example.carparts.util.SelectedVehicle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GenericUiScreensTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun cartScreen_showsTitleAndActions() {
        val cartTitle = targetContext.getString(R.string.cart_title)
        val checkoutLabel = targetContext.getString(R.string.btn_checkout)
        val backLabel = targetContext.getString(R.string.btn_back_to_parts)

        composeRule.setContent {
            CartScreen(
                innerPadding = PaddingValues(),
                items = listOf(
                    CartItem(
                        part = mapOf(
                            "PartId" to "31",
                            "Name" to "Timing Belt",
                            "Price" to "49.99",
                            "StockQuantity" to "6"
                        ),
                        quantity = 1
                    )
                ),
                onBackToParts = {},
                onCheckout = {},
                onIncreaseItem = {},
                onDecreaseItem = {}
            )
        }

        composeRule.onNodeWithText(cartTitle).assertIsDisplayed()
        composeRule.onNodeWithText(checkoutLabel).assertIsDisplayed()
        composeRule.onNodeWithText(backLabel).assertIsDisplayed()
    }

    @Test
    fun profileScreen_signOut_invokesCallback() {
        val signOutLabel = targetContext.getString(R.string.btn_sign_out)
        var didSignOut = false

        composeRule.setContent {
            ProfileScreen(
                innerPadding = PaddingValues(),
                isAdmin = false,
                selectedVehicle = null,
                onVehicleChanged = {},
                onSignOut = { didSignOut = true }
            )
        }

        composeRule.onNodeWithText(signOutLabel).performClick()
        assertTrue(didSignOut)
    }

    @Test
    fun profileScreen_removeVehicle_callsOnVehicleChangedWithNull() {
        val removeVehicleLabel = targetContext.getString(R.string.btn_remove_vehicle)
        var changedVehicle: SelectedVehicle? = SelectedVehicle("seed", "a", "b", "c", "d")

        composeRule.setContent {
            ProfileScreen(
                innerPadding = PaddingValues(),
                isAdmin = true,
                selectedVehicle = SelectedVehicle(
                    id = "44",
                    make = "BMW",
                    model = "X5",
                    year = "2022",
                    engineType = "Diesel"
                ),
                onVehicleChanged = { changedVehicle = it },
                onSignOut = {}
            )
        }

        composeRule.onNodeWithText(removeVehicleLabel).performClick()
        assertEquals(null, changedVehicle)
    }

    @Test
    fun vehiclePickerDialog_cancel_invokesDismiss() {
        val cancelLabel = targetContext.getString(R.string.btn_cancel)
        var dismissed = false

        composeRule.setContent {
            VehiclePickerDialog(
                onDismiss = { dismissed = true },
                onVehicleSelected = {}
            )
        }

        composeRule.onNodeWithText(cancelLabel).performClick()
        assertTrue(dismissed)
    }

    @Test
    fun vehiclePickerDialog_setVehicleButton_initiallyDisabledWithoutSelection() {
        val setVehicleLabel = targetContext.getString(R.string.btn_set_vehicle)

        composeRule.setContent {
            VehiclePickerDialog(
                onDismiss = {},
                onVehicleSelected = {}
            )
        }

        composeRule.onNodeWithText(setVehicleLabel).assertIsNotEnabled()
    }
}
