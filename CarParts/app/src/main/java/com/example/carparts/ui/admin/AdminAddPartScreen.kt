package com.example.carparts.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carparts.data.remote.SupaBaseClient
import com.example.carparts.util.getFirstNonBlank
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AdminAddPartContent(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onPartAdded: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var partNumber by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var stockQuantity by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var vehicleId by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var categoryOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var manufacturerOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var conditionOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var existingPartNumbers by remember { mutableStateOf<Set<String>>(emptySet()) }

    var vehicles by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var selectedMake by remember { mutableStateOf("") }
    var selectedModelDisplay by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        SupaBaseClient.fetchParts().onSuccess { parts ->
            categoryOptions = parts
                .mapNotNull { it.getFirstNonBlank("Category", "category") }
                .filter { it.isNotBlank() }.distinct().sorted()
            manufacturerOptions = parts
                .mapNotNull { it.getFirstNonBlank("Manufacturer", "manufacturer") }
                .filter { it.isNotBlank() }.distinct().sorted()
            conditionOptions = parts
                .mapNotNull { it.getFirstNonBlank("Condition", "condition") }
                .filter { it.isNotBlank() }.distinct().sorted()
            existingPartNumbers = parts
                .mapNotNull { it.getFirstNonBlank("PartNumber", "part_number", "sku") }
                .filter { it.isNotBlank() }
                .toSet()
        }
        SupaBaseClient.fetchVehicles().onSuccess { vehicles = it }
    }

    val makeOptions = vehicles
        .mapNotNull { it.getFirstNonBlank("Make", "make") }
        .filter { it.isNotBlank() }.distinct().sorted()

    val modelOptions = vehicles
        .filter { it.getFirstNonBlank("Make", "make").equals(selectedMake, ignoreCase = true) }
        .mapNotNull { v ->
            val id = v.getFirstNonBlank("VehicleId", "vehicleid") ?: return@mapNotNull null
            val model = v.getFirstNonBlank("Model", "model") ?: ""
            val year = v.getFirstNonBlank("Year", "year") ?: ""
            val display = if (year.isNotBlank()) "$model ($year)" else model
            Pair(display, id)
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1E3A8A)
                )
            }
            Text(
                text = "Add Part",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )
        }

        HorizontalDivider(color = Color(0xFFD1D5DB))

        TextField(
            value = name,
            onValueChange = { name = it; errorMessage = null },
            label = { Text("Name *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = price,
            onValueChange = { price = it; errorMessage = null },
            label = { Text("Price") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = partNumber,
            onValueChange = { partNumber = it },
            label = { Text("Part Number / SKU") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        AdminDropdownField(
            label = "Manufacturer",
            value = manufacturer,
            onValueChange = { manufacturer = it },
            options = manufacturerOptions
        )
        AdminDropdownField(
            label = "Category",
            value = category,
            onValueChange = { category = it },
            options = categoryOptions
        )
        AdminDropdownField(
            label = "Condition",
            value = condition,
            onValueChange = { condition = it },
            options = conditionOptions
        )
        TextField(
            value = stockQuantity,
            onValueChange = { stockQuantity = it },
            label = { Text("Stock Quantity") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Image URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        AdminDropdownField(
            label = "Vehicle Make",
            value = selectedMake,
            onValueChange = {
                selectedMake = it
                selectedModelDisplay = ""
                vehicleId = ""
            },
            options = makeOptions
        )
        AdminLabeledDropdownField(
            label = "Vehicle Model",
            displayValue = selectedModelDisplay,
            onValueChange = { display, id ->
                selectedModelDisplay = display
                vehicleId = id
            },
            options = modelOptions,
            enabled = selectedMake.isNotBlank() && modelOptions.isNotEmpty()
        )
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Text(text = errorMessage ?: "", color = Color(0xFFB91C1C))
        }

        Button(
            onClick = {
                if (name.isBlank()) {
                    errorMessage = "Name is required."
                    return@Button
                }
                val trimmedPartNumber = partNumber.trim()
                if (trimmedPartNumber.isNotBlank() &&
                    existingPartNumbers.any { it.equals(trimmedPartNumber, ignoreCase = true) }
                ) {
                    errorMessage = "Part Number \"$trimmedPartNumber\" already exists. Use a unique SKU."
                    return@Button
                }
                isLoading = true
                scope.launch {
                    val partData = buildMap {
                        put("Name", name.trim())
                        put("Price", price.trim())
                        put("PartNumber", partNumber.trim())
                        put("Manufacturer", manufacturer.trim())
                        put("Category", category.trim())
                        put("Condition", condition.trim())
                        put("StockQuantity", stockQuantity.trim())
                        put("Description", description.trim())
                        put("ImageUrl", imageUrl.trim())
                        put("VehicleId", vehicleId.trim())
                    }
                    SupaBaseClient.insertPart(partData)
                        .onSuccess {
                            onPartAdded("Part \"${name.trim()}\" added successfully.")
                            if (trimmedPartNumber.isNotBlank()) {
                                existingPartNumbers = existingPartNumbers + trimmedPartNumber
                            }
                            name = ""; price = ""; partNumber = ""; manufacturer = ""
                            category = ""; condition = ""; stockQuantity = ""
                            description = ""; imageUrl = ""
                            vehicleId = ""; selectedMake = ""; selectedModelDisplay = ""
                        }
                        .onFailure { errorMessage = it.message ?: "Failed to add part." }
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Adding..." else "Add Part")
        }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminDropdownField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && options.isNotEmpty(),
        onExpandedChange = { if (options.isNotEmpty()) expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = value,
            onValueChange = { onValueChange(it); expanded = false },
            label = { Text(label) },
            trailingIcon = {
                if (options.isNotEmpty()) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded && options.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onValueChange(option); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminLabeledDropdownField(
    label: String,
    displayValue: String,
    onValueChange: (display: String, id: String) -> Unit,
    options: List<Pair<String, String>>,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = {
                if (enabled) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (display, id) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = { onValueChange(display, id); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
