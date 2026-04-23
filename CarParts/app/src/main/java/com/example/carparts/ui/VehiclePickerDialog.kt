package com.example.carparts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carparts.data.remote.ApiClient
import com.example.carparts.util.SelectedVehicle
import com.example.carparts.util.getFirstNonBlank

@Composable
fun VehiclePickerDialog(
    onDismiss: () -> Unit,
    onVehicleSelected: (SelectedVehicle) -> Unit
) {
    var vehicles by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var selectedMake by remember { mutableStateOf<String?>(null) }
    var selectedModel by remember { mutableStateOf<String?>(null) }
    var selectedYear by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        ApiClient.fetchVehicles()
            .onSuccess { vehicles = it }
            .onFailure { error = it.message ?: "Failed to load vehicles." }
        isLoading = false
    }

    val availableMakes = remember(vehicles) {
        vehicles.mapNotNull { it.getFirstNonBlank("Make", "make") }.distinct().sorted()
    }
    val availableModels = remember(vehicles, selectedMake) {
        vehicles
            .filter { it.getFirstNonBlank("Make", "make")?.equals(selectedMake, ignoreCase = true) == true }
            .mapNotNull { it.getFirstNonBlank("Model", "model") }
            .distinct().sorted()
    }
    val availableYears = remember(vehicles, selectedMake, selectedModel) {
        vehicles
            .filter {
                it.getFirstNonBlank("Make", "make")?.equals(selectedMake, ignoreCase = true) == true &&
                it.getFirstNonBlank("Model", "model")?.equals(selectedModel, ignoreCase = true) == true
            }
            .mapNotNull { it.getFirstNonBlank("Year", "year")?.takeIf { y -> y.isNotBlank() } }
            .distinct().sortedDescending()
    }

    LaunchedEffect(availableModels) {
        if (availableModels.size == 1) selectedModel = availableModels[0]
        else if (selectedModel !in availableModels) selectedModel = null
    }
    LaunchedEffect(availableYears) {
        if (availableYears.size == 1) selectedYear = availableYears[0]
        else if (selectedYear !in availableYears) selectedYear = null
    }

    val matchingVehicle = remember(vehicles, selectedMake, selectedModel, selectedYear) {
        if (selectedMake == null || selectedModel == null) return@remember null
        vehicles.firstOrNull {
            it.getFirstNonBlank("Make", "make")?.equals(selectedMake, ignoreCase = true) == true &&
            it.getFirstNonBlank("Model", "model")?.equals(selectedModel, ignoreCase = true) == true &&
            (availableYears.isEmpty() || it.getFirstNonBlank("Year", "year")?.equals(selectedYear, ignoreCase = true) == true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Your Vehicle", fontWeight = FontWeight.Bold) },
        text = {
            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                error != null -> Text(
                    text = "Could not load vehicles: $error",
                    color = Color(0xFFB91C1C)
                )

                vehicles.isEmpty() -> Text(
                    text = "No vehicles available. Ask an admin to add vehicles.",
                    color = Color(0xFF6B7280)
                )

                else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    VehicleDropdown(
                        label = "Make",
                        selected = selectedMake,
                        options = availableMakes,
                        onSelected = {
                            selectedMake = it
                            selectedModel = null
                            selectedYear = null
                        },
                        enabled = true
                    )
                    VehicleDropdown(
                        label = "Model",
                        selected = selectedModel,
                        options = availableModels,
                        onSelected = {
                            selectedModel = it
                            selectedYear = null
                        },
                        enabled = selectedMake != null
                    )
                    if (availableYears.isNotEmpty()) {
                        VehicleDropdown(
                            label = "Year",
                            selected = selectedYear,
                            options = availableYears,
                            onSelected = { selectedYear = it },
                            enabled = selectedModel != null
                        )
                    }
                    matchingVehicle?.let { v ->
                        val engine = v.getFirstNonBlank("EngineType", "enginetype", "engine_type")
                        if (!engine.isNullOrBlank()) {
                            Text(
                                text = "Engine: $engine",
                                color = Color(0xFF4B5563),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val vehicle = matchingVehicle ?: return@Button
                    val id = vehicle.getFirstNonBlank("VehicleId", "vehicleid", "id") ?: return@Button
                    onVehicleSelected(
                        SelectedVehicle(
                            id = id,
                            make = selectedMake ?: "",
                            model = selectedModel ?: "",
                            year = selectedYear ?: "",
                            engineType = vehicle.getFirstNonBlank("EngineType", "enginetype") ?: ""
                        )
                    )
                },
                enabled = matchingVehicle != null
            ) {
                Text("Set Vehicle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleDropdown(
    label: String,
    selected: String?,
    options: List<String>,
    onSelected: (String) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled && options.isNotEmpty(),
        onExpandedChange = { if (enabled && options.isNotEmpty()) expanded = it }
    ) {
        OutlinedTextField(
            value = selected ?: "Select $label",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            enabled = enabled && options.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
