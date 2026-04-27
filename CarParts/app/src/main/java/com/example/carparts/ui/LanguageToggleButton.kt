package com.example.carparts.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carparts.R
import androidx.core.os.LocaleListCompat

@Composable
fun LanguageToggleButton(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    LocalConfiguration.current
    val locales = AppCompatDelegate.getApplicationLocales()
    val isIrish = !locales.isEmpty && locales[0]?.language == "ga"

    Button(
        onClick = {
            if (isIrish) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            } else {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ga"))
            }
        },
        modifier = modifier.testTag("language_toggle_button"),
        shape = RoundedCornerShape(if (compact) 18.dp else 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
    ) {
        Text(
            text = if (compact) {
                if (isIrish) stringResource(R.string.label_lang_short_en)
                else stringResource(R.string.label_lang_short_ga)
            } else {
                stringResource(R.string.btn_switch_language)
            },
            fontWeight = FontWeight.SemiBold
        )
    }
}
