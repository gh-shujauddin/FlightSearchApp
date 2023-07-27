@file:OptIn(ExperimentalMaterial3Api::class)

package com.qadri.flightsearch

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qadri.flightsearch.ui.screen.FlightSearchViewModel
import com.qadri.flightsearch.ui.screen.HomeScreen

@Composable
fun FlightSearchApp(
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Flight Search",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(MaterialTheme.colorScheme.primary)
            )
        }
    ) { innerPadding ->
        val viewModel: FlightSearchViewModel = viewModel(factory = FlightSearchViewModel.Factory)
        HomeScreen(
            modifier = Modifier.padding(innerPadding),
            viewModel = viewModel
        )
    }
}

