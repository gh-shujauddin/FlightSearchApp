@file:OptIn(ExperimentalMaterial3Api::class)

package com.qadri.flightsearch.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qadri.flightsearch.R
import com.qadri.flightsearch.data.Favorite

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: FlightSearchViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val suggestions by viewModel.getQueryFromDb.collectAsState()
    val allAirport by viewModel.getAllAirport.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val selectedAirport by viewModel.airportState.collectAsState()
    val favoriteList by viewModel.getAllFavorite.collectAsState()

    val searchHistory by viewModel.searchHistoryFlow.collectAsState()

    val focusRequester = remember { FocusRequester() }  //To remove the focus of textField on click
    val focusManager = LocalFocusManager.current

    val interactionSource =
        remember { MutableInteractionSource() } // To make no clickable effect of LazyColumn

//    LaunchedEffect(true) {
//        viewModel.getAllFavorites()
//    }


    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.searchString,
            onValueChange = viewModel::onSearchTextChange,
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .focusRequester(focusRequester),
            placeholder = {
                Text(text = "Search for flight")
            },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.extraLarge,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )

        if (uiState.isFlightClicked) {
            val context = LocalContext.current
            val displayToast = { text: String ->
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            }
            Text(
                text = stringResource(R.string.flight_from, selectedAirport.iataCode),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable(interactionSource = interactionSource, indication = null) {
                        focusManager.clearFocus()
                    },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(allAirport.allAirport) { destination ->
                    if (destination.toAirportState(destination) != selectedAirport) {
                        FlightCard(
                            depart = selectedAirport,
                            arrive = destination.toAirportState(destination),
                            isFavorite = false,
                            onClick = {
                                viewModel.insertIntoFavorite(
                                    Favorite(
                                        0,
                                        selectedAirport.iataCode,
                                        destination.iataCode
                                    )
                                )
                                displayToast("Added To Favorite")
                            }
                        )
                    }
                }
            }
        } else {
            if (uiState.searchString.isEmpty() && searchHistory.isNullOrBlank()) {
                FavoriteScreen(
                    favoriteList = favoriteList,
                    modifier = modifier,
                    interactionSource = interactionSource,
                    focusManager = focusManager,
                    allAirport = allAirport,
                    onClick = {
                        viewModel.deleteFromFavorite(
                            it
                        )
                    }
                )
            } else {
                SuggestionList(
                    isSearching = isSearching,
                    suggestions = suggestions,
                    focusManager = focusManager,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun FavoriteScreen(
    favoriteList: AllFavoriteList,
    modifier: Modifier,
    interactionSource: MutableInteractionSource,
    focusManager: FocusManager,
    allAirport: AllAirportList,
    onClick: (Favorite) -> Unit
) {
    val context = LocalContext.current
    val displayToast = { text: String ->
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
    Text(
        text = stringResource(R.string.favorite_routes),
        modifier = Modifier.padding(16.dp),
        style = MaterialTheme.typography.titleMedium,
        fontSize = 20.sp
    )
    if (favoriteList.allFavorite.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = "Nothing to show", modifier = modifier.align(Alignment.Center))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .clickable(interactionSource = interactionSource, indication = null) {
                    focusManager.clearFocus()
                },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(favoriteList.allFavorite, key = { it.id }) { favorite ->
                val departureAirport = allAirport.allAirport.find {
                    it.iataCode == favorite.departureCode
                }
                val destinationAirport = allAirport.allAirport.find {
                    it.iataCode == favorite.destinationCode
                }
//                        val airport by viewModel.airport.collectAsState()
                departureAirport?.let { departure ->
                    destinationAirport?.let { destination ->
                        FlightCard(
                            depart = departure.toAirportState(departure),
                            arrive = destination.toAirportState(destination),
                            isFavorite = true,
                            onClick = {
                                onClick(favorite)
                                displayToast("Removed from favorites")
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SuggestionList(
    isSearching: Boolean,
    suggestions: SearchedList,
    focusManager: FocusManager,
    viewModel: FlightSearchViewModel
) {
    if (isSearching) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items(suggestions.searchedList) { text ->
                Text(
                    text = buildAnnotatedString {
                        pushStyle(SpanStyle(fontWeight = FontWeight(500)))
                        append(text.iataCode)
                        pop()
                        append("  ")
                        append(text.name)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable {
                            viewModel.onSearchTextChange(
                                searchString = text.iataCode,
                                isFlightClicked = true,
                                airportState = text.toAirportState(text)
                            )
                            focusManager.clearFocus()
                        }
                )
            }
        }
    }
}

@Composable
fun FlightCard(
    depart: AirportState,
    arrive: AirportState,
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(intrinsicSize = IntrinsicSize.Max)
        ) {
            Column(
                modifier = Modifier
                    .weight(5f)
                    .fillMaxHeight()
            ) {
                Text(text = "DEPART", fontWeight = FontWeight(400))
                Spacer(modifier = modifier.height(4.dp))
                FlightDetails(airportState = depart)
                Spacer(modifier = modifier.height(16.dp))
                Text(text = "ARRIVE", fontWeight = FontWeight(400))
                Spacer(modifier = modifier.height(4.dp))
                FlightDetails(airportState = arrive)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable {
                            onClick()
                        },
                    tint = if (isFavorite) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            }
        }
    }
}

@Composable
fun FlightDetails(airportState: AirportState) {
    Row {
        Text(
            text = airportState.iataCode,
            fontWeight = FontWeight(500),
            style = MaterialTheme.typography.titleMedium,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = airportState.name)
    }
}

@Preview(showBackground = true)
@Composable
fun CardPreview() {
    FlightCard(
        depart = AirportState(id = 1, iataCode = "ADF", "InterAirport", 25),
        arrive = AirportState(id = 2, iataCode = "KDK", "firs Airport", 25),
        isFavorite = true,
        onClick = {}
    )
}