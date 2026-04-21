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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch

@Composable
internal fun AdminAddVehicleContent(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onVehicleAdded: (String) -> Unit
) {
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var engineType by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

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
                text = "Add Vehicle",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )
        }

        HorizontalDivider(color = Color(0xFFD1D5DB))

        TextField(
            value = make,
            onValueChange = { make = it; errorMessage = null },
            label = { Text("Make *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = model,
            onValueChange = { model = it; errorMessage = null },
            label = { Text("Model *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Year") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = engineType,
            onValueChange = { engineType = it },
            label = { Text("Engine Type") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
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

        if (errorMessage != null) {
            Text(text = errorMessage ?: "", color = Color(0xFFB91C1C))
        }

        Button(
            onClick = {
                if (make.isBlank() || model.isBlank()) {
                    errorMessage = "Make and Model are required."
                    return@Button
                }
                isLoading = true
                scope.launch {
                    val vehicleData = buildMap {
                        put("Make", make.trim())
                        put("Model", model.trim())
                        put("Year", year.trim())
                        put("EngineType", engineType.trim())
                        put("Category", category.trim())
                        put("ImageUrl", imageUrl.trim())
                    }
                    SupaBaseClient.insertVehicle(vehicleData)
                        .onSuccess {
                            onVehicleAdded("Vehicle \"${make.trim()} ${model.trim()}\" added successfully.")
                            make = ""; model = ""; year = ""; engineType = ""; category = ""; imageUrl = ""
                        }
                        .onFailure { errorMessage = it.message ?: "Failed to add vehicle." }
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Adding..." else "Add Vehicle")
        }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
