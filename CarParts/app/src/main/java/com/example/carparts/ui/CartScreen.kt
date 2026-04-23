package com.example.carparts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carparts.CartItem
import com.example.carparts.R
import com.example.carparts.util.basketKey
import com.example.carparts.util.getFirstNonBlank
import com.example.carparts.util.toPriceLabel

@Composable
fun CartScreen(
    innerPadding: PaddingValues,
    items: List<CartItem>,
    onBackToParts: () -> Unit,
    onCheckoutTest: () -> Unit,
    onIncreaseItem: (String) -> Unit,
    onDecreaseItem: (String) -> Unit
) {
    val subtotal = items.sumOf { item ->
        val price = item.part.getFirstNonBlank("Price", "price")?.toDoubleOrNull() ?: 0.0
        price * item.quantity
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.cart_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E3A8A)
        )

        if (items.isEmpty()) {
            Text(text = stringResource(R.string.cart_empty), color = Color(0xFF4B5563))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items) { item ->
                    val title = item.part.getFirstNonBlank("Name", "name", "title") ?: "Part"
                    val key = item.part.basketKey()
                    val priceText = item.part.getFirstNonBlank("Price", "price").toPriceLabel()

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                            Text(
                                stringResource(R.string.label_cart_price, priceText),
                                color = Color(0xFF374151)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(onClick = { onDecreaseItem(key) }) { Text("-") }
                                Text(
                                    stringResource(R.string.label_qty, item.quantity),
                                    color = Color(0xFF111827)
                                )
                                TextButton(onClick = { onIncreaseItem(key) }) { Text("+") }
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFD1D5DB))
        Text(
            text = stringResource(R.string.label_subtotal, "%.2f".format(subtotal)),
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )
        Button(
            onClick = onCheckoutTest,
            enabled = items.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_checkout))
        }
        TextButton(onClick = onBackToParts, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.btn_back_to_parts))
        }
    }
}
