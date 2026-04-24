package com.example.carparts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carparts.R
import kotlinx.coroutines.delay

@Composable
fun CheckoutSuccessScreen(
    innerPadding: PaddingValues,
    onContinueShopping: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2500)
        onContinueShopping()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.checkout_thanks_title),
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E3A8A)
        )
        Text(
            text = stringResource(R.string.checkout_redirect_message),
            fontSize = 15.sp,
            color = Color(0xFF4B5563),
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        Button(
            onClick = onContinueShopping,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.btn_continue_shopping))
        }
    }
}
