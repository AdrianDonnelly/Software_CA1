package com.example.carparts.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carparts.R

@Composable
fun CarPartsTopBar(
    cartItemCount: Int,
    selectedCategory: String?,
    isAdmin: Boolean,
    onOpenCategoryDrawer: () -> Unit,
    onOpenCart: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        color = Color(0xFFF8FAFC)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenCategoryDrawer) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.cd_browse_categories),
                    tint = Color(0xFF1E3A8A)
                )
            }

            Text(
                text = stringResource(R.string.app_title),
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
                            Badge { Text(text = cartItemCount.toString(), fontSize = 10.sp) }
                        }
                    }
                ) {
                    IconButton(onClick = onOpenCart) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = stringResource(R.string.cd_open_cart),
                            tint = Color(0xFF1E3A8A)
                        )
                    }
                }

                if (isAdmin) {
                    Spacer(modifier = Modifier.size(8.dp))
                    IconButton(onClick = onOpenAdmin) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.cd_admin_panel),
                            tint = Color(0xFF1E3A8A)
                        )
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))
                LanguageToggleButton(compact = true)
                Spacer(modifier = Modifier.size(8.dp))

                IconButton(onClick = onOpenProfile) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(R.string.cd_view_profile),
                        tint = Color(0xFF1E3A8A)
                    )
                }
            }
        }
    }
}
