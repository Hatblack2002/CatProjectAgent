package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.cat.CatProjectAgentApp
import com.example.ui.theme.CatProjectAgentTheme
import com.example.viewmodel.CatProjectViewModel

class MainActivity : ComponentActivity() {

    private val catViewModel: CatProjectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CatProjectAgentTheme(darkTheme = true) {
                CatProjectAgentApp(viewModel = catViewModel)
            }
        }
    }
}
