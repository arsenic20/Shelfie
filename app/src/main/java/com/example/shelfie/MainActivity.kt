package com.example.shelfie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.shelfie.ui.navigation.ShelfieNavGraph
import com.example.shelfie.ui.theme.ShelfieTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShelfieTheme {
                ShelfieNavGraph()
            }
        }
    }
}