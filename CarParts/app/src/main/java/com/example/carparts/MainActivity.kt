package com.example.carparts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.material3.MenuAnchorType
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

private const val ADMIN_EMAIL = "admin@carparts.com"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CarPartsTheme {
                var isAuthenticated by remember { mutableStateOf(false) }
                var isAdmin by remember { mutableStateOf(false) }
                var selectedCategory by remember { mutableStateOf<String?>(null) }
                var availableCategories by remember { mutableStateOf<List<String>>(emptyList()) }
                var currentScreen by remember { mutableStateOf(HomeScreen.PARTS) }
                var isCategoryDrawerOpen by remember { mutableStateOf(false) }
                val basket = remember { mutableStateMapOf<String, CartItem>() }
                val basketCount = basket.values.sumOf { it.quantity }
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            if (isAuthenticated) {
                                CarPartsTopBar(
                                    cartItemCount = basketCount,
                                    selectedCategory = selectedCategory,
                                    isAdmin = isAdmin,
                                    onOpenCategoryDrawer = { isCategoryDrawerOpen = true },
                                    onOpenCart = { currentScreen = HomeScreen.CART },
                                    onOpenAdmin = { currentScreen = HomeScreen.ADMIN },
                                    onSignOut = {
                                        scope.launch {
                                            AuthRepository.signOut()
                                            isAuthenticated = false
                                            isAdmin = false
                                            selectedCategory = null
                                            currentScreen = HomeScreen.PARTS
                                            isCategoryDrawerOpen = false
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
                                    isAdmin = AuthRepository.getCurrentUserEmail()
                                        .equals(ADMIN_EMAIL, ignoreCase = true)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(message)
                                    }
                                }
                            )
                        } else {
                            if (currentScreen == HomeScreen.ADMIN) {
                                AdminScreen(
                                    innerPadding = innerPadding,
                                    onBack = { currentScreen = HomeScreen.PARTS },
                                    onMessage = { message ->
                                        scope.launch { snackbarHostState.showSnackbar(message) }
                                    }
                                )
                            } else if (currentScreen == HomeScreen.CART) {
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

                    if (isAuthenticated) {
                        AnimatedVisibility(
                            visible = isCategoryDrawerOpen,
                            enter = slideInHorizontally(initialOffsetX = { -it }),
                            exit = slideOutHorizontally(targetOffsetX = { -it })
                        ) {
                            CategoryDrawer(
                                categories = availableCategories,
                                selectedCategory = selectedCategory,
                                onCategorySelected = { cat ->
                                    selectedCategory = cat
                                    currentScreen = HomeScreen.PARTS
                                    isCategoryDrawerOpen = false
                                },
                                onClose = { isCategoryDrawerOpen = false }
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
    CART,
    ADMIN
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
    isAdmin: Boolean,
    onOpenCategoryDrawer: () -> Unit,
    onOpenCart: () -> Unit,
    onOpenAdmin: () -> Unit,
    onSignOut: () -> Unit
) {
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
            IconButton(onClick = onOpenCategoryDrawer) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Browse categories",
                    tint = Color(0xFF1E3A8A)
                )
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

                if (isAdmin) {
                    Spacer(modifier = Modifier.size(8.dp))
                    IconButton(
                        onClick = onOpenAdmin,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCEAF7))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Admin panel",
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

private enum class AdminView { MENU, ADD_PART, ADD_VEHICLE }

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
            onAddVehicle = { adminView = AdminView.ADD_VEHICLE }
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
    }
}

@Composable
private fun AdminMenuContent(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onAddPart: () -> Unit,
    onAddVehicle: () -> Unit
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
                    contentDescription = "Back",
                    tint = Color(0xFF1E3A8A)
                )
            }
            Text(
                text = "Admin Panel",
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
                Text("Add Part", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF111827))
                Text("Add a new car part to the catalogue", color = Color(0xFF4B5563))
            }
        }

        Card(
            onClick = onAddVehicle,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Add Vehicle", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF111827))
                Text("Add a new vehicle make and model", color = Color(0xFF4B5563))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Parts")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminAddPartContent(
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

@Composable
private fun AdminAddVehicleContent(
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
                    onClick = {
                        onValueChange(display, id)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
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
                if (options.isNotEmpty()) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
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
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun CategoryDrawer(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Surface(
            tonalElevation = 0.dp,
            color = Color(0xFF1E3A8A)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shop by Category",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                CategoryDrawerItem(
                    label = "All Parts",
                    isSelected = selectedCategory == null,
                    onClick = { onCategorySelected(null) }
                )
                HorizontalDivider(color = Color(0xFFE5E7EB))
            }
            items(categories) { category ->
                CategoryDrawerItem(
                    label = category,
                    isSelected = selectedCategory == category,
                    onClick = { onCategorySelected(category) }
                )
                HorizontalDivider(color = Color(0xFFE5E7EB))
            }
        }
    }
}

@Composable
private fun CategoryDrawerItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) Color(0xFFEFF6FF) else Color.White)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF111827),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF9CA3AF)
        )
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