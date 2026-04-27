package com.example.carparts

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.carparts.ui.AuthScreen
import com.example.carparts.ui.CartScreen
import com.example.carparts.ui.PartRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CartAuthCheckoutUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun authLogin_showsRequiredMessage_whenFieldsBlank() {
        val requiredMessage = targetContext.getString(R.string.error_email_password_required)
        val loginLabel = targetContext.getString(R.string.btn_login)

        composeRule.setContent {
            AuthScreen(
                innerPadding = PaddingValues(),
                onAuthSuccess = {}
            )
        }

        composeRule.onNodeWithText(loginLabel).performClick()
        composeRule.onNodeWithText(requiredMessage).assertIsDisplayed()
    }

    @Test
    fun authLogin_showsInvalidEmailMessage_forInvalidEmail() {
        val emailLabel = targetContext.getString(R.string.label_email)
        val passwordLabel = targetContext.getString(R.string.label_password)
        val loginLabel = targetContext.getString(R.string.btn_login)
        val invalidEmailMessage = targetContext.getString(R.string.error_invalid_email)

        composeRule.setContent {
            AuthScreen(
                innerPadding = PaddingValues(),
                onAuthSuccess = {}
            )
        }

        composeRule.onNode(hasText(emailLabel) and hasSetTextAction()).performTextInput("invalid-email")
        composeRule.onNode(hasText(passwordLabel) and hasSetTextAction()).performTextInput("abcdef")
        composeRule.onNodeWithText(loginLabel).performClick()
        composeRule.onNodeWithText(invalidEmailMessage).assertIsDisplayed()
    }

    @Test
    fun partRow_addToBasket_invokesCallback() {
        val addLabel = targetContext.getString(R.string.btn_add_to_basket)
        var addClicks = 0

        composeRule.setContent {
            PartRow(
                part = mapOf(
                    "Name" to "Brake Pad",
                    "Price" to "19.99",
                    "StockQuantity" to "5",
                    "PartNumber" to "BP-1",
                    "Category" to "Brakes"
                ),
                onClick = {},
                onAddToBasket = { addClicks++ },
                canAddToBasket = true
            )
        }

        composeRule.onNodeWithText(addLabel).performClick()
        assertEquals(1, addClicks)
    }

    @Test
    fun cartCheckout_isDisabled_whenCartEmpty() {
        val checkoutLabel = targetContext.getString(R.string.btn_checkout)

        composeRule.setContent {
            CartScreen(
                innerPadding = PaddingValues(),
                items = emptyList(),
                onBackToParts = {},
                onCheckout = {},
                onIncreaseItem = {},
                onDecreaseItem = {}
            )
        }

        composeRule.onNodeWithText(checkoutLabel).assertIsNotEnabled()
    }

    @Test
    fun cartCheckout_invokesCallback_whenCartHasItems() {
        val checkoutLabel = targetContext.getString(R.string.btn_checkout)
        var didCheckout = false

        composeRule.setContent {
            CartScreen(
                innerPadding = PaddingValues(),
                items = listOf(
                    CartItem(
                        part = mapOf(
                            "PartId" to "1",
                            "Name" to "Air Filter",
                            "Price" to "25.00",
                            "StockQuantity" to "3"
                        ),
                        quantity = 1
                    )
                ),
                onBackToParts = {},
                onCheckout = { didCheckout = true },
                onIncreaseItem = {},
                onDecreaseItem = {}
            )
        }

        composeRule.onNodeWithText(checkoutLabel).performClick()
        assertTrue(didCheckout)
    }
}
