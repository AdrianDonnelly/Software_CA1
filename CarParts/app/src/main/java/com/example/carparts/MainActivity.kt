package com.example.carparts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carparts.data.remote.SupaBaseClient
import com.example.carparts.ui.theme.CarPartsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CarPartsTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { CarPartsTopBar() }
                ) { innerPadding ->
                    PartsScreen(innerPadding = innerPadding)
                }
            }
        }
    }
}

@Composable
fun PartsScreen(innerPadding: PaddingValues) {
    var parts by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null

        SupaBaseClient.fetchParts()
            .onSuccess { fetchedParts ->
                parts = fetchedParts
            }
            .onFailure { error ->
                errorMessage = error.message ?: "Failed to load parts from Supabase."
            }

        isLoading = false
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
                Text(
                    text = errorMessage ?: "Something went wrong.",
                    color = Color(0xFFB91C1C)
                )
            }
        }

        parts.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No parts found in your Supabase table.")
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
                items(parts) { part ->
                    PartRow(part = part)
                }
            }
        }
    }
}

@Composable
fun PartRow(part: Map<String, String>) {
    val title = part.getFirstNonBlank("name", "part_name", "partname", "title")
        ?: "Unnamed part"
    val price = part.getFirstNonBlank("price", "cost", "unit_price")
    val stock = part.getFirstNonBlank("stock", "quantity", "qty", "inventory")
    val sku = part.getFirstNonBlank("sku", "part_number", "part_no", "id")
    val imageUrl = part.getFirstNonBlank("ImageUrl", "image_url", "imageurl")

    val detailRows = part
        .filterKeys { key ->
            key.lowercase() !in setOf(
                "name", "part_name", "partname", "title",
                "price", "cost", "unit_price",
                "stock", "quantity", "qty", "inventory",
                "sku", "part_number", "part_no", "id",
                "imageurl", "image_url"
            )
        }
        .entries
        .take(4)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
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
                Text(
                    text = "Price: ${price ?: "-"}",
                    color = Color(0xFF1F2937)
                )

                Text(
                    text = "Stock: ${stock ?: "-"}",
                    color = Color(0xFF1F2937)
                )
            }

            if (sku != null) {
                Text(
                    text = "SKU: $sku",
                    color = Color(0xFF1F2937)
                )
            }

            if (detailRows.isNotEmpty()) {
                HorizontalDivider(color = Color(0xFFD1D5DB))
                detailRows.forEach { (key, value) ->
                    Text(
                        text = "$key: $value",
                        color = Color(0xFF374151)
                    )
                }
            }
        }
    }
}

private fun Map<String, String>.getFirstNonBlank(vararg keys: String): String? {
    return keys.firstNotNullOfOrNull { key ->
        this.entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value
            ?.takeIf { it.isNotBlank() }
    }
}

@Composable
fun CarPartsTopBar() {
    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        color = Color(0xFFF8FAFC)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color(0xFF1E3A8A)
                )
            }

            Text(
                text = "CarParts",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A),
                letterSpacing = (-0.5).sp,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCEAF7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF1E3A8A)
                )
            }
        }
    }
}