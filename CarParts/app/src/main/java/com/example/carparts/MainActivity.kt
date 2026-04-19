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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carparts.data.remote.AuthRepository
import com.example.carparts.data.remote.SupaBaseClient
import com.example.carparts.ui.theme.CarPartsTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CarPartsTheme {
                var isAuthenticated by remember { mutableStateOf(false) }
                var selectedCategory by remember { mutableStateOf<String?>(null) }
                var availableCategories by remember { mutableStateOf<List<String>>(emptyList()) }
                var currentScreen by remember { mutableStateOf(HomeScreen.PARTS) }
                val basket = remember { mutableStateMapOf<String, CartItem>() }
                val basketCount = basket.values.sumOf { it.quantity }
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (isAuthenticated) {
                            CarPartsTopBar(
                                cartItemCount = basketCount,
                                selectedCategory = selectedCategory,
                                categories = availableCategories,
                                onCategorySelected = {
                                    selectedCategory = it
                                    currentScreen = HomeScreen.PARTS
                                },
                                onOpenCart = { currentScreen = HomeScreen.CART },
                                onSignOut = {
                                    scope.launch {
                                        AuthRepository.signOut()
                                        isAuthenticated = false
                                        selectedCategory = null
                                        currentScreen = HomeScreen.PARTS
                                        basket.clear()
                                        snackbarHostState.showSnackbar("Signed out")
                                    }
                                }
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    if (!isAuthenticated) {
                        AuthScreen(
                            innerPadding = innerPadding,
                            onAuthSuccess = { message ->
                                isAuthenticated = true
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    } else {
                        if (currentScreen == HomeScreen.CART) {
                            CartScreen(
                                innerPadding = innerPadding,
                                items = basket.values.toList(),
                                onBackToParts = { currentScreen = HomeScreen.PARTS },
                                onCheckoutTest = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Checkout (test) clicked. No DB stock updates yet."
                                        )
                                    }
                                },
                                onIncreaseItem = { key ->
                                    basket[key]?.let { item ->
                                        basket[key] = item.copy(quantity = item.quantity + 1)
                                    }
                                },
                                onDecreaseItem = { key ->
                                    basket[key]?.let { item ->
                                        val updatedQuantity = item.quantity - 1
                                        if (updatedQuantity <= 0) {
                                            basket.remove(key)
                                        } else {
                                            basket[key] = item.copy(quantity = updatedQuantity)
                                        }
                                    }
                                }
                            )
                        } else {
                            PartsScreen(
                                innerPadding = innerPadding,
                                selectedCategory = selectedCategory,
                                onCategoriesLoaded = { availableCategories = it },
                                onAddToBasket = { part ->
                                    val key = part.basketKey()
                                    val existing = basket[key]
                                    basket[key] = if (existing == null) {
                                        CartItem(part = part, quantity = 1)
                                    } else {
                                        existing.copy(quantity = existing.quantity + 1)
                                    }
                                    val title = part.getFirstNonBlank("Name", "name", "title") ?: "Part"
                                    scope.launch {
                                        snackbarHostState.showSnackbar("$title added to basket")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class CartItem(
    val part: Map<String, String>,
    val quantity: Int
)

private enum class HomeScreen {
    PARTS,
    CART
}

@Composable
fun AuthScreen(
    innerPadding: PaddingValues,
    onAuthSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun validateInputs(): Boolean {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password are required."
            return false
        }
        if (!email.contains("@")) {
            errorMessage = "Enter a valid email."
            return false
        }
        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters."
            return false
        }
        return true
    }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Welcome to CarParts",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "Sign in or create an account to continue.",
                    color = Color(0xFF4B5563)
                )

                TextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFB91C1C)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (!validateInputs()) return@Button
                            isLoading = true
                            scope.launch {
                                AuthRepository.signIn(email.trim(), password)
                                    .onSuccess { onAuthSuccess("Signed in successfully") }
                                    .onFailure { errorMessage = it.message ?: "Sign in failed." }
                                isLoading = false
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Login")
                    }

                    Button(
                        onClick = {
                            if (!validateInputs()) return@Button
                            isLoading = true
                            scope.launch {
                                AuthRepository.signUp(email.trim(), password)
                                    .onSuccess { onAuthSuccess("Account created. You are logged in.") }
                                    .onFailure { errorMessage = it.message ?: "Sign up failed." }
                                isLoading = false
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                    ) {
                        Text("Sign Up")
                    }
                }
            }
        }
    }
}

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
                Text(
                    text = errorMessage ?: "Something went wrong.",
                    color = Color(0xFFB91C1C)
                )
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
    val title = part.getFirstNonBlank("Name", "name", "part_name", "partname", "title")
        ?: "Unnamed part"
    val price = part.getFirstNonBlank("Price", "price", "cost", "unit_price")
    val stockQuantity = part.readStockQuantity()
    val sku = part.getFirstNonBlank("PartNumber", "sku", "part_number", "part_no", "id")
    val brand = part.getFirstNonBlank("Manufacturer", "manufacturer")
    val category = part.getFirstNonBlank("Category", "category")
    val condition = part.getFirstNonBlank("Condition", "condition")
    val imageUrl = part.getFirstNonBlank("ImageUrl", "image_url", "imageurl")

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        ),
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
                Text(
                    text = "Price: ${price.toPriceLabel()}",
                    color = Color(0xFF1F2937)
                )

                Text(
                    text = stockQuantity.toStockLabel(),
                    color = Color(0xFF1F2937)
                )
            }

            if (sku != null) {
                Text(
                    text = "SKU: $sku",
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
    val title = part.getFirstNonBlank("Name", "name", "part_name", "partname", "title")
        ?: "Part details"
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
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            Button(
                onClick = onAddToBasket,
                enabled = canAddToBasket
            ) {
                Text(if (canAddToBasket) "Add to Basket (Test)" else "Out of Stock")
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
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

private fun Map<String, String>.getFirstNonBlank(vararg keys: String): String? {
    return keys.firstNotNullOfOrNull { key ->
        this.entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value
            ?.takeIf { it.isNotBlank() }
    }
}

@Composable
fun CarPartsTopBar(
    cartItemCount: Int,
    selectedCategory: String?,
    categories: List<String>,
    onCategorySelected: (String?) -> Unit,
    onOpenCart: () -> Unit,
    onSignOut: () -> Unit
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }

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
            Box {
                IconButton(onClick = { categoryMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Choose category",
                        tint = Color(0xFF1E3A8A)
                    )
                }

                DropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All parts") },
                        onClick = {
                            onCategorySelected(null)
                            categoryMenuExpanded = false
                        }
                    )
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                onCategorySelected(category)
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Text(
                text = selectedCategory?.let { "CarParts - $it" } ?: "CarParts",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A),
                letterSpacing = (-0.5).sp,
                modifier = Modifier.weight(1f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge {
                                Text(
                                    text = cartItemCount.toString(),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = onOpenCart,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCEAF7))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Open cart",
                            tint = Color(0xFF1E3A8A)
                        )
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))

                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCEAF7))
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Sign out",
                        tint = Color(0xFF1E3A8A)
                    )
                }
            }
        }
    }
}

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
            text = "Cart",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E3A8A)
        )

        if (items.isEmpty()) {
            Text(text = "Your cart is empty (test).", color = Color(0xFF4B5563))
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
                            Text("Price: $priceText", color = Color(0xFF374151))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(onClick = { onDecreaseItem(key) }) { Text("-") }
                                Text("Qty: ${item.quantity}", color = Color(0xFF111827))
                                TextButton(onClick = { onIncreaseItem(key) }) { Text("+") }
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFD1D5DB))
        Text(
            text = "Subtotal (test): $${"%.2f".format(subtotal)}",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )
        Button(
            onClick = onCheckoutTest,
            enabled = items.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Checkout (test)")
        }
        TextButton(onClick = onBackToParts, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Parts")
        }
    }
}

private fun Map<String, String>.readCategoryName(): String? {
    return getFirstNonBlank("Category", "category")
        ?.trim()
        ?.takeIf { it.isNotBlank() }
}

private fun Map<String, String>.matchesCategory(selectedCategory: String): Boolean {
    return readCategoryName()?.equals(selectedCategory, ignoreCase = true) == true
}

private fun Map<String, String>.basketKey(): String {
    return getFirstNonBlank("PartId", "partid", "id", "PartNumber", "sku", "Name", "name")
        ?: "unknown-part"
}

private fun Map<String, String>.readStockQuantity(): Int {
    return getFirstNonBlank("StockQuantity", "stockquantity", "stock", "quantity", "qty", "inventory")
        ?.toIntOrNull()
        ?.coerceAtLeast(0)
        ?: 0
}

private fun String?.toPriceLabel(): String {
    val number = this?.toDoubleOrNull()
    return if (number == null) "-" else "$%.2f".format(number)
}

private fun Int.toStockLabel(): String {
    return when {
        this <= 0 -> "Out of stock"
        this <= 5 -> "Low stock ($this)"
        else -> "In stock ($this)"
    }
}