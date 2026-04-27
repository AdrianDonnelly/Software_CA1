package com.example.carparts

import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assert
import androidx.core.os.LocaleListCompat
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.carparts.ui.AuthScreen
import com.example.carparts.ui.LanguageToggleButton
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguageChangeButtonEspressoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun resetToDefaultLocale() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
        Espresso.onIdle()
    }

    @After
    fun tearDownLocale() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
        Espresso.onIdle()
    }

    private fun waitForText(text: String, timeoutMillis: Long = 5_000L) {
        composeRule.waitUntil(timeoutMillis) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun languageButton_compact_flipsGaAndEnLabels() {
        composeRule.setContent {
            LanguageToggleButton(compact = true)
        }

        composeRule.onNodeWithTag("language_toggle_button").assertIsDisplayed()
        composeRule.onNodeWithTag("language_toggle_button").assert(hasText("GA"))

        composeRule.onNodeWithTag("language_toggle_button").performClick()
        Espresso.onIdle()
        composeRule.waitForIdle()
        waitForText("EN")

        composeRule.onNodeWithTag("language_toggle_button").assert(hasText("EN"))

        composeRule.onNodeWithTag("language_toggle_button").performClick()
        Espresso.onIdle()
        composeRule.waitForIdle()
        waitForText("GA")

        composeRule.onNodeWithTag("language_toggle_button").assert(hasText("GA"))
    }

    @Test
    fun authScreen_languageButton_togglesWelcomeTitle() {
        composeRule.setContent {
            AuthScreen(
                innerPadding = PaddingValues(),
                onAuthSuccess = {}
            )
        }

        composeRule.onNodeWithText("Welcome to CarParts").assertIsDisplayed()

        composeRule.onNodeWithTag("language_toggle_button").performClick()
        Espresso.onIdle()
        composeRule.waitForIdle()
        waitForText("Fáilte go CarParts")

        composeRule.onNodeWithText("Fáilte go CarParts").assertIsDisplayed()

        composeRule.onNodeWithTag("language_toggle_button").performClick()
        Espresso.onIdle()
        composeRule.waitForIdle()
        waitForText("Welcome to CarParts")

        composeRule.onNodeWithText("Welcome to CarParts").assertIsDisplayed()
    }
}
