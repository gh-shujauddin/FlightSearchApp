package com.qadri.flightsearch.data

import kotlinx.coroutines.flow.Flow

interface FlightSearchRepository {
    fun getItemFromAirport(name: String): Flow<List<Airport>>

    fun getAllAirport(): Flow<List<Airport>>

    fun getAllFavorites(): Flow<List<Favorite>>

    suspend fun insertIntoFavorite(favorite: Favorite)

    suspend fun deleteFromFavorite(favorite: Favorite)

}