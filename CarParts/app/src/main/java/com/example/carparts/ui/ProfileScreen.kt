package com.example.carparts.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carparts.data.remote.AuthRepository

@Composable
fun ProfileScreen(
    innerPadding: PaddingValues,
    isAdmin: Boolean,
    onSignOut: () -> Unit
) {
    val profile = remember { AuthRepository.getCurrentUserProfile() }
    val email = profile?.email ?: "Unknown"
    val initial = email.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val shortId = profile?.id?.take(8)?.uppercase() ?: "-"
    val memberSince = profile?.createdAt?.take(10) ?: "-"
    val lastSignIn = profile?.lastSignInAt?.take(10) ?: "-"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E3A8A)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Text(
            text = email,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )

        if (isAdmin) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E3A8A))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(text = "Admin", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        HorizontalDivider(color = Color(0xFFE5E7EB))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Account Details", fontWeight = FontWeight.Bold, color = Color(0xFF111827), fontSize = 16.sp)

                HorizontalDivider(color = Color(0xFFE5E7EB))

                ProfileRow(label = "Email", value = email)
                ProfileRow(label = "Account ID", value = shortId)
                ProfileRow(label = "Member Since", value = memberSince)
                ProfileRow(label = "Last Sign In", value = lastSignIn)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
            Text("Sign Out", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFF6B7280), fontSize = 14.sp)
        Text(text = value, color = Color(0xFF111827), fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
