package com.example.carparts.ui.admin

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carparts.R
import com.example.carparts.data.remote.ApiClient
import com.example.carparts.util.getFirstNonBlank
import kotlinx.coroutines.launch

@Composable
internal fun AdminDeletePartContent(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onMessage: (String) -> Unit
) {
    var parts by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var partToDelete by remember { mutableStateOf<Map<String, String>?>(null) }
    var isDeleting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        ApiClient.fetchParts()
            .onSuccess { parts = it.sortedBy { p -> p.getFirstNonBlank("Name") ?: "" } }
            .onFailure { errorMessage = it.message }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = Color(0xFF1E3A8A)
                )
            }
            Text(
                text = stringResource(R.string.admin_delete_part_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )
        }

        HorizontalDivider(color = Color(0xFFD1D5DB))
        Spacer(modifier = Modifier.height(8.dp))

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            errorMessage != null -> Text(errorMessage ?: "", color = Color(0xFFB91C1C))
            parts.isEmpty() -> Text(stringResource(R.string.no_parts_found), color = Color(0xFF6B7280))
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    parts,
                    key = { it.getFirstNonBlank("PartId") }
                ) { part ->
                    val name = part.getFirstNonBlank("Name") ?: "-"
                    val sku = part.getFirstNonBlank("PartNumber")
                    val category = part.getFirstNonBlank("Category")

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                                if (sku != null || category != null) {
                                    Text(
                                        listOfNotNull(sku, category).joinToString(" • "),
                                        color = Color(0xFF6B7280),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            IconButton(onClick = { partToDelete = part }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.btn_delete),
                                    tint = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    partToDelete?.let { part ->
        val name = part.getFirstNonBlank("Name") ?: "-"
        AlertDialog(
            onDismissRequest = { if (!isDeleting) partToDelete = null },
            title = {
                Text(
                    stringResource(R.string.admin_delete_confirm_title, name),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.admin_delete_confirm_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        val id = part.getFirstNonBlank("PartId") ?: return@Button
                        isDeleting = true
                        scope.launch {
                            ApiClient.deletePart(id)
                                .onSuccess {
                                    parts = parts.filter {
                                        it.getFirstNonBlank("PartId") != id
                                    }
                                    onMessage(context.getString(R.string.msg_part_deleted, name))
                                }
                                .onFailure {
                                    errorMessage = it.message
                                        ?: context.getString(R.string.error_delete_part_failed)
                                }
                            isDeleting = false
                            partToDelete = null
                        }
                    },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(
                        if (isDeleting) stringResource(R.string.btn_deleting)
                        else stringResource(R.string.btn_delete)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { partToDelete = null }, enabled = !isDeleting) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
internal fun AdminDeleteVehicleContent(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onMessage: (String) -> Unit
) {
    var vehicles by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var vehicleToDelete by remember { mutableStateOf<Map<String, String>?>(null) }
    var isDeleting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        ApiClient.fetchVehicles()
            .onSuccess { vehicles = it.sortedBy { v -> v.getFirstNonBlank("Make") ?: "" } }
            .onFailure { errorMessage = it.message }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = Color(0xFF1E3A8A)
                )
            }
            Text(
                text = stringResource(R.string.admin_delete_vehicle_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )
        }

        HorizontalDivider(color = Color(0xFFD1D5DB))
        Spacer(modifier = Modifier.height(8.dp))

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            errorMessage != null -> Text(errorMessage ?: "", color = Color(0xFFB91C1C))
            vehicles.isEmpty() -> Text(stringResource(R.string.no_vehicles_found), color = Color(0xFF6B7280))
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    vehicles,
                    key = { it.getFirstNonBlank("VehicleId") }
                ) { vehicle ->
                    val make = vehicle.getFirstNonBlank("Make") ?: "-"
                    val model = vehicle.getFirstNonBlank("Model") ?: ""
                    val year = vehicle.getFirstNonBlank("Year") ?: ""
                    val displayName = listOfNotNull(
                        year.takeIf { it.isNotBlank() },
                        make,
                        model.takeIf { it.isNotBlank() }
                    ).joinToString(" ")

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                displayName,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { vehicleToDelete = vehicle }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.btn_delete),
                                    tint = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    vehicleToDelete?.let { vehicle ->
        val make = vehicle.getFirstNonBlank("Make") ?: "-"
        val model = vehicle.getFirstNonBlank("Model") ?: ""
        val displayName = "$make $model".trim()
        AlertDialog(
            onDismissRequest = { if (!isDeleting) vehicleToDelete = null },
            title = {
                Text(
                    stringResource(R.string.admin_delete_confirm_title, displayName),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.admin_delete_confirm_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        val id = vehicle.getFirstNonBlank("VehicleId") ?: return@Button
                        isDeleting = true
                        scope.launch {
                            ApiClient.deleteVehicle(id)
                                .onSuccess {
                                    vehicles = vehicles.filter {
                                        it.getFirstNonBlank("VehicleId") != id
                                    }
                                    onMessage(context.getString(R.string.msg_vehicle_deleted, displayName))
                                }
                                .onFailure {
                                    errorMessage = it.message
                                        ?: context.getString(R.string.error_delete_vehicle_failed)
                                }
                            isDeleting = false
                            vehicleToDelete = null
                        }
                    },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(
                        if (isDeleting) stringResource(R.string.btn_deleting)
                        else stringResource(R.string.btn_delete)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { vehicleToDelete = null }, enabled = !isDeleting) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}
