package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.UserPreferencesDataStore
import com.example.data.local.WellnessDatabase
import com.example.data.repository.WellnessRepository
import com.example.ui.MindBodyApp
import com.example.ui.theme.MindBodyTheme
import com.example.ui.viewmodel.WellnessViewModel
import com.example.ui.viewmodel.WellnessViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = WellnessDatabase.getInstance(applicationContext)
        val preferences = UserPreferencesDataStore(applicationContext)
        val repository = WellnessRepository(database.wellnessDao(), preferences)
        val factory = WellnessViewModelFactory(repository)

        setContent {
            val wellnessViewModel: WellnessViewModel = viewModel(factory = factory)
            val uiState by wellnessViewModel.uiState.collectAsStateWithLifecycle()

            MindBodyTheme(
                themeMode = uiState.preferences.themeMode,
                reducedMotion = uiState.preferences.reducedMotion
            ) {
                MindBodyApp(viewModel = wellnessViewModel)
            }
        }
    }
}
