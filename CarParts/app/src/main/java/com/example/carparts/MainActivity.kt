package com.example.carparts

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.carparts.data.remote.AuthRepository
import com.example.carparts.ui.AuthScreen
import com.example.carparts.ui.CarPartsTopBar
import com.example.carparts.ui.CartScreen
import com.example.carparts.ui.CategoryDrawer
import com.example.carparts.ui.PartsScreen
import com.example.carparts.ui.ProfileScreen
import com.example.carparts.ui.admin.AdminScreen
import androidx.compose.ui.res.stringResource
import com.example.carparts.ui.theme.CarPartsTheme
import com.example.carparts.util.VehiclePreferences
import com.example.carparts.util.basketKey
import com.example.carparts.util.getFirstNonBlank
import kotlinx.coroutines.launch

private const val ADMIN_EMAIL = "admin@carparts.com"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val context = this

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
                val msgSignedOut = stringResource(R.string.msg_signed_out)
                val msgCheckoutTest = stringResource(R.string.msg_checkout_test)
                var selectedVehicle by remember { mutableStateOf(VehiclePreferences.load(context)) }

                Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        topBar = {
                            if (isAuthenticated) {
                                CarPartsTopBar(
                                    cartItemCount = basketCount,
                                    selectedCategory = selectedCategory,
                                    isAdmin = isAdmin,
                                    onOpenCategoryDrawer = { isCategoryDrawerOpen = true },
                                    onOpenCart = { currentScreen = HomeScreen.CART },
                                    onOpenAdmin = { currentScreen = HomeScreen.ADMIN },
                                    onOpenProfile = { currentScreen = HomeScreen.PROFILE }
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
                                    selectedVehicle = VehiclePreferences.load(context)
                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                }
                            )
                        } else {
                            when (currentScreen) {
                                HomeScreen.PROFILE -> ProfileScreen(
                                    innerPadding = innerPadding,
                                    isAdmin = isAdmin,
                                    selectedVehicle = selectedVehicle,
                                    onVehicleChanged = { vehicle ->
                                        selectedVehicle = vehicle
                                        if (vehicle != null) VehiclePreferences.save(context, vehicle)
                                        else VehiclePreferences.clear(context)
                                    },
                                    onSignOut = {
                                        scope.launch {
                                            AuthRepository.signOut()
                                            isAuthenticated = false
                                            isAdmin = false
                                            selectedCategory = null
                                            selectedVehicle = null
                                            VehiclePreferences.clear(context)
                                            currentScreen = HomeScreen.PARTS
                                            isCategoryDrawerOpen = false
                                            basket.clear()
                                            snackbarHostState.showSnackbar(msgSignedOut)
                                        }
                                    }
                                )
                                HomeScreen.ADMIN -> AdminScreen(
                                    innerPadding = innerPadding,
                                    onBack = { currentScreen = HomeScreen.PARTS },
                                    onMessage = { message ->
                                        scope.launch { snackbarHostState.showSnackbar(message) }
                                    }
                                )
                                HomeScreen.CART -> CartScreen(
                                    innerPadding = innerPadding,
                                    items = basket.values.toList(),
                                    onBackToParts = { currentScreen = HomeScreen.PARTS },
                                    onCheckoutTest = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(msgCheckoutTest)
                                        }
                                    },
                                    onIncreaseItem = { key ->
                                        basket[key]?.let { item ->
                                            basket[key] = item.copy(quantity = item.quantity + 1)
                                        }
                                    },
                                    onDecreaseItem = { key ->
                                        basket[key]?.let { item ->
                                            val newQty = item.quantity - 1
                                            if (newQty <= 0) basket.remove(key)
                                            else basket[key] = item.copy(quantity = newQty)
                                        }
                                    }
                                )
                                HomeScreen.PARTS -> PartsScreen(
                                    innerPadding = innerPadding,
                                    selectedCategory = selectedCategory,
                                    selectedVehicle = selectedVehicle,
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
                                            snackbarHostState.showSnackbar(
                                                context.getString(R.string.msg_added_to_basket, title)
                                            )
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

internal enum class HomeScreen {
    PARTS,
    CART,
    ADMIN,
    PROFILE
}
