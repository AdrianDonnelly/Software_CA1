package com.example.carparts

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.carparts.data.remote.SupaBaseClient
import com.example.carparts.ui.theme.CarPartsTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("APP_TEST", "onCreate started")

        enableEdgeToEdge()

        lifecycleScope.launch {
            try {
                SupaBaseClient.testVehicles()
                Log.d("SUPABASE", "testVehicles ran")
            } catch (e: Exception) {
                Log.e("SUPABASE", "Error: ${e.message}")
            }
        }

        setContent {
            CarPartsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Text(
                        text = "APP IS RUNNING",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}