package com.qadri.flightsearch.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qadri.flightsearch.FlightSearchApplication
import com.qadri.flightsearch.data.Airport
import com.qadri.flightsearch.data.Favorite
import com.qadri.flightsearch.data.FlightSearchRepository
import com.qadri.flightsearch.data.UserPreferenceRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoriteAirport(
    val departureCode: Airport = AirportState().toAirport(AirportState()),
    val destinationCode: Airport = AirportState().toAirport(AirportState())
)


class FlightSearchViewModel(
    private val flightSearchRepository: FlightSearchRepository,
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlightSearchUiState())
    var uiState = _uiState.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    var isSearching = _isSearching.asStateFlow()

    private val _airportState = MutableStateFlow(AirportState())
    val airportState = _airportState.asStateFlow()

    var getQueryFromDb: StateFlow<SearchedList> = MutableStateFlow(SearchedList())

    private val _getAllAirport = MutableStateFlow(AllAirportList())
    val getAllAirport = flightSearchRepository.getAllAirport()
        .map {
            AllAirportList(it)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = _getAllAirport.value
        )

    private val _getAllFavorite = MutableStateFlow(AllFavoriteList())
    val getAllFavorite = flightSearchRepository.getAllFavorites()
        .map {
            AllFavoriteList(it)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = _getAllFavorite.value
        )

    fun insertIntoFavorite(favorite: Favorite) {
        viewModelScope.launch {
            flightSearchRepository.insertIntoFavorite(favorite)
        }
    }

    fun deleteFromFavorite(favorite: Favorite) {
        viewModelScope.launch {
            flightSearchRepository.deleteFromFavorite(favorite)
        }
    }

//    private val _suggestions = MutableStateFlow(option)
//    val suggestions = uiState
//        .onEach { _isSearching.update { true } }
//        .combine(_suggestions) { text, option ->
//            if (text.searchString.isNotEmpty()) {
//                option.filter {
//                    delay(100)
//                    it.doesMatchSearchQuery(text.searchString)
//                }
//
//            } else {
//                listOf<AirportState>()
//            }
//        }
//        .onEach { _isSearching.update { false } }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = _suggestions.value
//        )

// Implementation of the filtering text from the list
//    private val _searchTextState = MutableStateFlow("")
//    var searchTextState = _searchTextState.asStateFlow()
//
//    private val _options = MutableStateFlow(option)
//    val options: StateFlow<List<SearchString>> = searchTextState
//        .debounce(500L)
//        .onEach { _isSearching.update { true } }
//        .combine(_options) { text, option ->
//            if (text.isNotEmpty()) {
//                option.filter {
//                    delay(100)
//                    it.doesMatchSearchQuery(text)
//                }
//            } else {
//                listOf<SearchString>()
//            }
//        }
//        .onEach { _isSearching.update { false } }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = _options.value
//        )

    fun onSearchTextChange(searchString: String) {
        _uiState.value = FlightSearchUiState().copy(searchString = searchString)
        getQueryFromDb = flightSearchRepository.getItemFromAirport(searchString)
            .filterNotNull()
            .onEach { _isSearching.update { true } }
            .map {
                if (_uiState.value.searchString.isNotEmpty()) {
                    delay(500L)
                    SearchedList(it)
                } else {
                    SearchedList(listOf())
                }
            }
            .onEach { _isSearching.update { false } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = SearchedList()
            )
        saveSearchHistory(searchString)
    }

    fun onSearchTextChange(searchString: String, isFlightClicked: Boolean) {
        _uiState.value = FlightSearchUiState().copy(
            searchString = searchString,
            isFlightClicked = isFlightClicked
        )
    }

    fun onSearchTextChange(
        searchString: String,
        isFlightClicked: Boolean,
        airportState: AirportState
    ) {
        _uiState.value =
            FlightSearchUiState(searchString = searchString, isFlightClicked = isFlightClicked)
        _airportState.value = airportState
        saveSearchHistory(searchString)
    }

    val searchHistoryFlow = userPreferenceRepository.searchedHistory
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            ""
        )

    private fun saveSearchHistory(searchString: String) {
        viewModelScope.launch {
            userPreferenceRepository.saveSearchedHistory(searchString)
        }
    }

    init {
        viewModelScope.launch {
            val savedSearchString = userPreferenceRepository.searchedHistory.first()
            onSearchTextChange(savedSearchString ?: "")
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FlightSearchApplication)
                FlightSearchViewModel(
                    application.container.flightSearchRepository,
                    application.userPreferenceRepository
                )
            }
        }
    }
}


data class AirportState(
    val id: Int = 0,
    val iataCode: String = "",
    val name: String = "",
    val passengers: Int = 0
)

//data class SearchString(
//    val search: String
//) {
//    fun doesMatchSearchQuery(query: String): Boolean {
//        val matchingCombinations = listOf(
//            search,
//            "${search.first()}",
//        )
//        return matchingCombinations.any {
//            it.contains(query, ignoreCase = true)
//        }
//    }
//}

fun Airport.toAirportState(airport: Airport): AirportState {
    return AirportState(
        id = airport.id,
        iataCode = airport.iataCode,
        name = airport.name,
        passengers = airport.passengers
    )
}

fun AirportState.toAirport(airportState: AirportState): Airport {
    return Airport(
        id = airportState.id,
        name = airportState.name,
        iataCode = airportState.iataCode,
        passengers = airportState.passengers
    )
}

data class FlightSearchUiState(
    var searchString: String = "",
    var isFlightClicked: Boolean = false,
    var searchHistory: List<String> = listOf()
)

data class SearchedList(
    var searchedList: List<Airport> = listOf()
)

data class AllAirportList(
    val allAirport: List<Airport> = listOf()
)

data class AllFavoriteList(
    val allFavorite: List<Favorite> = listOf()
)