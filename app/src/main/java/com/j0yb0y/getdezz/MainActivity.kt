package com.j0yb0y.getdezz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.j0yb0y.getdezz.ui.MainScreen
import com.j0yb0y.getdezz.ui.MainViewModel
import com.j0yb0y.getdezz.ui.theme.GetDezzTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as GetDezzApp
        val viewModel = ViewModelProvider(this, MainViewModel.Factory(app))[MainViewModel::class.java]

        setContent {
            GetDezzTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
