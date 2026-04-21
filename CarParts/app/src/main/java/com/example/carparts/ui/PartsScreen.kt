package com.example.carparts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carparts.data.remote.SupaBaseClient
import com.example.carparts.util.getFirstNonBlank
import com.example.carparts.util.matchesCategory
import com.example.carparts.util.readCategoryName
import com.example.carparts.util.readStockQuantity
import com.example.carparts.util.toPriceLabel
import com.example.carparts.util.toStockLabel

@Composable
fun PartsScreen(
    innerPadding: PaddingValues,
    selectedCategory: String?,
    onCategoriesLoaded: (List<String>) -> Unit,
    onAddToBasket: (Map<String, String>) -> Unit
) {
    var parts by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedPart by remember { mutableStateOf<Map<String, String>?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null

        SupaBaseClient.fetchParts()
            .onSuccess { fetchedParts ->
                parts = fetchedParts.shuffled()
                onCategoriesLoaded(
                    fetchedParts.mapNotNull { it.readCategoryName() }
                        .distinct()
                        .sorted()
                )
            }
            .onFailure { error ->
                errorMessage = error.message ?: "Failed to load parts from Supabase."
            }

        isLoading = false
    }

    val visibleParts = if (selectedCategory.isNullOrBlank()) {
        parts
    } else {
        parts.filter { it.matchesCategory(selectedCategory) }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        errorMessage != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = errorMessage ?: "Something went wrong.", color = Color(0xFFB91C1C))
            }
        }

        visibleParts.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val message = if (selectedCategory.isNullOrBlank()) {
                    "No parts found in your Supabase table."
                } else {
                    "No parts found in \"$selectedCategory\"."
                }
                Text(text = message)
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = selectedCategory ?: "All Parts",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = Color(0xFFE5E7EB))
                }
                items(visibleParts) { part ->
                    val stockQuantity = part.readStockQuantity()
                    PartRow(
                        part = part,
                        onClick = { selectedPart = part },
                        onAddToBasket = { onAddToBasket(part) },
                        canAddToBasket = stockQuantity > 0
                    )
                }
            }
        }
    }

    selectedPart?.let { part ->
        val stockQuantity = part.readStockQuantity()
        PartDetailsDialog(
            part = part,
            onDismiss = { selectedPart = null },
            onAddToBasket = { onAddToBasket(part) },
            canAddToBasket = stockQuantity > 0
        )
    }
}

@Composable
fun PartRow(
    part: Map<String, String>,
    onClick: () -> Unit,
    onAddToBasket: () -> Unit,
    canAddToBasket: Boolean
) {
    val title = part.getFirstNonBlank("Name", "name", "part_name", "partname", "title") ?: "Unnamed part"
    val price = part.getFirstNonBlank("Price", "price", "cost", "unit_price")
    val stockQuantity = part.readStockQuantity()
    val sku = part.getFirstNonBlank("PartNumber", "sku", "part_number", "part_no", "id")
    val brand = part.getFirstNonBlank("Manufacturer", "manufacturer")
    val category = part.getFirstNonBlank("Category", "category")
    val condition = part.getFirstNonBlank("Condition", "condition")
    val imageUrl = part.getFirstNonBlank("ImageUrl", "image_url", "imageurl")

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (part.isEmpty()) {
                Text(text = "Empty row")
                return@Column
            }

            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = title,
                color = Color(0xFF111827),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Price: ${price.toPriceLabel()}", color = Color(0xFF1F2937))
                Text(text = stockQuantity.toStockLabel(), color = Color(0xFF1F2937))
            }

            if (sku != null) {
                Text(text = "SKU: $sku", color = Color(0xFF1F2937))
            }

            if (brand != null || category != null || condition != null) {
                Text(
                    text = listOfNotNull(brand, category, condition).joinToString(" • "),
                    color = Color(0xFF4B5563),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onAddToBasket,
                enabled = canAddToBasket,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (canAddToBasket) "Add to Basket (Test)" else "Out of Stock")
            }
        }
    }
}

@Composable
fun PartDetailsDialog(
    part: Map<String, String>,
    onDismiss: () -> Unit,
    onAddToBasket: () -> Unit,
    canAddToBasket: Boolean
) {
    val title = part.getFirstNonBlank("Name", "name", "part_name", "partname", "title") ?: "Part details"
    val partNumber = part.getFirstNonBlank("PartNumber", "part_number", "sku")
    val category = part.getFirstNonBlank("Category", "category")
    val manufacturer = part.getFirstNonBlank("Manufacturer", "manufacturer")
    val condition = part.getFirstNonBlank("Condition", "condition")
    val description = part.getFirstNonBlank("Description", "description")
    val vehicleId = part.getFirstNonBlank("VehicleId", "vehicleid")
    val partId = part.getFirstNonBlank("PartId", "partid", "id")
    val stockQuantity = part.readStockQuantity()
    val price = part.getFirstNonBlank("Price", "price")
    val imageUrl = part.getFirstNonBlank("ImageUrl", "image_url", "imageurl")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = {
            Button(onClick = onAddToBasket, enabled = canAddToBasket) {
                Text(if (canAddToBasket) "Add to Basket (Test)" else "Out of Stock")
            }
        },
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                HorizontalDivider(color = Color(0xFFD1D5DB))
                Text("Product Info", fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                Text("SKU: ${partNumber ?: "-"}", color = Color(0xFF374151))
                Text("Brand: ${manufacturer ?: "-"}", color = Color(0xFF374151))
                Text("Category: ${category ?: "-"}", color = Color(0xFF374151))
                Text("Condition: ${condition ?: "-"}", color = Color(0xFF374151))
                Text("Part ID: ${partId ?: "-"}", color = Color(0xFF374151))

                HorizontalDivider(color = Color(0xFFD1D5DB))
                Text("Pricing & Stock", fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                Text("Price: ${price.toPriceLabel()}", color = Color(0xFF374151))
                Text(stockQuantity.toStockLabel(), color = Color(0xFF374151))

                HorizontalDivider(color = Color(0xFFD1D5DB))
                Text("Compatibility", fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                Text("Vehicle ID: ${vehicleId ?: "-"}", color = Color(0xFF374151))

                if (!description.isNullOrBlank()) {
                    HorizontalDivider(color = Color(0xFFD1D5DB))
                    Text("Description", fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    Text(description, color = Color(0xFF374151))
                }
            }
        }
    )
}
