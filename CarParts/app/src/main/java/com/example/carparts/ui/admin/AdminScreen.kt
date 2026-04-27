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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carparts.R

internal enum class AdminView { MENU, ADD_PART, ADD_VEHICLE, DELETE_PART, DELETE_VEHICLE }

@Composable
fun AdminScreen(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onMessage: (String) -> Unit
) {
    var adminView by remember { mutableStateOf(AdminView.MENU) }

    when (adminView) {
        AdminView.MENU -> AdminMenuContent(
            innerPadding = innerPadding,
            onBack = onBack,
            onAddPart = { adminView = AdminView.ADD_PART },
            onAddVehicle = { adminView = AdminView.ADD_VEHICLE },
            onDeletePart = { adminView = AdminView.DELETE_PART },
            onDeleteVehicle = { adminView = AdminView.DELETE_VEHICLE }
        )
        AdminView.ADD_PART -> AdminAddPartContent(
            innerPadding = innerPadding,
            onBack = { adminView = AdminView.MENU },
            onPartAdded = onMessage
        )
        AdminView.ADD_VEHICLE -> AdminAddVehicleContent(
            innerPadding = innerPadding,
            onBack = { adminView = AdminView.MENU },
            onVehicleAdded = onMessage
        )
        AdminView.DELETE_PART -> AdminDeletePartContent(
            innerPadding = innerPadding,
            onBack = { adminView = AdminView.MENU },
            onMessage = onMessage
        )
        AdminView.DELETE_VEHICLE -> AdminDeleteVehicleContent(
            innerPadding = innerPadding,
            onBack = { adminView = AdminView.MENU },
            onMessage = onMessage
        )
    }
}

@Composable
private fun AdminMenuContent(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onAddPart: () -> Unit,
    onAddVehicle: () -> Unit,
    onDeletePart: () -> Unit,
    onDeleteVehicle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                text = stringResource(R.string.admin_panel_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )
        }

        HorizontalDivider(color = Color(0xFFD1D5DB))

        Card(
            onClick = onAddPart,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.admin_add_part_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF111827)
                )
                Text(stringResource(R.string.admin_add_part_desc), color = Color(0xFF4B5563))
            }
        }

        Card(
            onClick = onAddVehicle,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.admin_add_vehicle_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF111827)
                )
                Text(stringResource(R.string.admin_add_vehicle_desc), color = Color(0xFF4B5563))
            }
        }

        Card(
            onClick = onDeletePart,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.admin_delete_part_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFDC2626)
                )
                Text(stringResource(R.string.admin_delete_part_desc), color = Color(0xFF4B5563))
            }
        }

        Card(
            onClick = onDeleteVehicle,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.admin_delete_vehicle_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFDC2626)
                )
                Text(stringResource(R.string.admin_delete_vehicle_desc), color = Color(0xFF4B5563))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.btn_back_to_parts))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
