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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carparts.R
import com.example.carparts.data.remote.ApiClient
import com.example.carparts.util.SelectedVehicle
import com.example.carparts.util.getFirstNonBlank
import com.example.carparts.util.matchesCategory
import com.example.carparts.util.readCategoryName
import com.example.carparts.util.readStockQuantity
import com.example.carparts.util.toPriceLabel

@Composable
fun PartsScreen(
    innerPadding: PaddingValues,
    selectedCategory: String?,
    selectedVehicle: SelectedVehicle?,
    onCategoriesLoaded: (List<String>) -> Unit,
    onAddToBasket: (Map<String, String>) -> Unit
) {
    var parts by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedPart by remember { mutableStateOf<Map<String, String>?>(null) }
    var filterByVehicle by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(selectedVehicle) {
        if (selectedVehicle == null) filterByVehicle = false
    }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        ApiClient.fetchParts()
            .onSuccess { fetchedParts ->
                parts = fetchedParts.shuffled()
                onCategoriesLoaded(
                    fetchedParts.mapNotNull { it.readCategoryName() }.distinct().sorted()
                )
            }
            .onFailure { errorMessage = it.message ?: "Failed to load parts." }
        isLoading = false
    }

    val q = searchQuery.trim().lowercase()
    val visibleParts = parts.filter { part ->
        (selectedCategory.isNullOrBlank() || part.matchesCategory(selectedCategory)) &&
        (!filterByVehicle || selectedVehicle == null || part.getFirstNonBlank("VehicleId") == selectedVehicle.id) &&
        (q.isEmpty() ||
            part.getFirstNonBlank("Name")?.lowercase()?.contains(q) == true ||
            part.getFirstNonBlank("PartNumber")?.lowercase()?.contains(q) == true ||
            part.getFirstNonBlank("Manufacturer")?.lowercase()?.contains(q) == true ||
            part.getFirstNonBlank("Category")?.lowercase()?.contains(q) == true)
    }

    when {
        isLoading -> Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        errorMessage != null -> Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            contentAlignment = Alignment.Center
        ) { Text(text = errorMessage ?: "", color = Color(0xFFB91C1C)) }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedVehicle != null) {
                    item {
                        VehicleBanner(
                            vehicle = selectedVehicle,
                            filterEnabled = filterByVehicle,
                            onToggleFilter = { filterByVehicle = !filterByVehicle }
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(stringResource(R.string.hint_search_parts)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    val heading = when {
                        filterByVehicle && selectedVehicle != null ->
                            stringResource(R.string.heading_parts_for_vehicle, selectedVehicle.displayName)
                        !selectedCategory.isNullOrBlank() -> selectedCategory
                        else -> stringResource(R.string.all_parts)
                    }
                    Text(
                        text = heading,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = Color(0xFFE5E7EB))
                }

                if (visibleParts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val message = when {
                                searchQuery.isNotBlank() ->
                                    stringResource(R.string.no_parts_matching_search, searchQuery.trim())
                                filterByVehicle && selectedVehicle != null ->
                                    stringResource(R.string.no_parts_for_vehicle, selectedVehicle.displayName)
                                !selectedCategory.isNullOrBlank() ->
                                    stringResource(R.string.no_parts_in_category, selectedCategory)
                                else -> stringResource(R.string.no_parts_found)
                            }
                            Text(text = message, color = Color(0xFF6B7280))
                        }
                    }
                } else {
                    items(visibleParts) { part ->
                        PartRow(
                            part = part,
                            onClick = { selectedPart = part },
                            onAddToBasket = { onAddToBasket(part) },
                            canAddToBasket = part.readStockQuantity() > 0
                        )
                    }
                }
            }
        }
    }

    selectedPart?.let { part ->
        PartDetailsDialog(
            part = part,
            onDismiss = { selectedPart = null },
            onAddToBasket = { onAddToBasket(part) },
            canAddToBasket = part.readStockQuantity() > 0
        )
    }
}

@Composable
private fun VehicleBanner(
    vehicle: SelectedVehicle,
    filterEnabled: Boolean,
    onToggleFilter: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (filterEnabled) Color(0xFF1E3A8A) else Color(0xFFEFF6FF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.my_vehicle),
                    fontSize = 11.sp,
                    color = if (filterEnabled) Color.White.copy(alpha = 0.7f) else Color(0xFF4B5563)
                )
                Text(
                    text = vehicle.displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (filterEnabled) Color.White else Color(0xFF1E3A8A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = onToggleFilter,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (filterEnabled) Color.White else Color(0xFF1E3A8A)
                )
            ) {
                Text(
                    text = if (filterEnabled) stringResource(R.string.btn_remove_filter)
                    else stringResource(R.string.btn_filter_parts),
                    color = if (filterEnabled) Color(0xFF1E3A8A) else Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun PartRow(
    part: Map<String, String>,
    onClick: () -> Unit,
    onAddToBasket: () -> Unit,
    canAddToBasket: Boolean
) {
    val title = part.getFirstNonBlank("Name") ?: "Unnamed part"
    val price = part.getFirstNonBlank("Price")
    val stockQuantity = part.readStockQuantity()
    val sku = part.getFirstNonBlank("PartNumber")
    val brand = part.getFirstNonBlank("Manufacturer")
    val category = part.getFirstNonBlank("Category")
    val condition = part.getFirstNonBlank("Condition")
    val imageUrl = part.getFirstNonBlank("ImageUrl")

    val stockLabel = when {
        stockQuantity <= 0 -> stringResource(R.string.label_out_of_stock)
        stockQuantity <= 5 -> stringResource(R.string.label_low_stock, stockQuantity)
        else -> stringResource(R.string.label_in_stock, stockQuantity)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (part.isEmpty()) {
                Text(text = stringResource(R.string.empty_row))
                return@Column
            }

            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
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
                Text(
                    text = stringResource(R.string.label_price_prefix, price.toPriceLabel()),
                    color = Color(0xFF1F2937)
                )
                Text(text = stockLabel, color = Color(0xFF1F2937))
            }

            if (sku != null) {
                Text(
                    text = stringResource(R.string.label_sku_prefix, sku),
                    color = Color(0xFF1F2937)
                )
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
                Text(
                    if (canAddToBasket) stringResource(R.string.btn_add_to_basket)
                    else stringResource(R.string.btn_out_of_stock)
                )
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
    val title = part.getFirstNonBlank("Name") ?: "Part details"
    val partNumber = part.getFirstNonBlank("PartNumber")
    val category = part.getFirstNonBlank("Category")
    val manufacturer = part.getFirstNonBlank("Manufacturer")
    val condition = part.getFirstNonBlank("Condition")
    val description = part.getFirstNonBlank("Description")
    val vehicleId = part.getFirstNonBlank("VehicleId")
    val partId = part.getFirstNonBlank("PartId")
    val stockQuantity = part.readStockQuantity()
    val price = part.getFirstNonBlank("Price")
    val imageUrl = part.getFirstNonBlank("ImageUrl")

    val stockLabel = when {
        stockQuantity <= 0 -> stringResource(R.string.label_out_of_stock)
        stockQuantity <= 5 -> stringResource(R.string.label_low_stock, stockQuantity)
        else -> stringResource(R.string.label_in_stock, stockQuantity)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_close)) }
        },
        dismissButton = {
            Button(onClick = onAddToBasket, enabled = canAddToBasket) {
                Text(
                    if (canAddToBasket) stringResource(R.string.btn_add_to_basket)
                    else stringResource(R.string.btn_out_of_stock)
                )
            }
        },
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                HorizontalDivider(color = Color(0xFFD1D5DB))
                Text(stringResource(R.string.section_product_info), fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                Text(stringResource(R.string.detail_sku, partNumber ?: "-"), color = Color(0xFF374151))
                Text(stringResource(R.string.detail_brand, manufacturer ?: "-"), color = Color(0xFF374151))
                Text(stringResource(R.string.detail_category, category ?: "-"), color = Color(0xFF374151))
                Text(stringResource(R.string.detail_condition, condition ?: "-"), color = Color(0xFF374151))
                Text(stringResource(R.string.detail_part_id, partId ?: "-"), color = Color(0xFF374151))

                HorizontalDivider(color = Color(0xFFD1D5DB))
                Text(stringResource(R.string.section_pricing_stock), fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                Text(stringResource(R.string.detail_price, price.toPriceLabel()), color = Color(0xFF374151))
                Text(stockLabel, color = Color(0xFF374151))

                HorizontalDivider(color = Color(0xFFD1D5DB))
                Text(stringResource(R.string.section_compatibility), fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                Text(stringResource(R.string.detail_vehicle_id, vehicleId ?: "-"), color = Color(0xFF374151))

                if (!description.isNullOrBlank()) {
                    HorizontalDivider(color = Color(0xFFD1D5DB))
                    Text(stringResource(R.string.section_description), fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    Text(description, color = Color(0xFF374151))
                }
            }
        }
    )
}
