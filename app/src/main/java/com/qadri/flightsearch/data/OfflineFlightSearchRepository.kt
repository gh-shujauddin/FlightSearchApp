package com.qadri.flightsearch.data

import kotlinx.coroutines.flow.Flow

class OfflineFlightSearchRepository(private val flightDao: FlightDao) : FlightSearchRepository {

    override fun getItemFromAirport(name: String): Flow<List<Airport>> = flightDao.getItemFromAirport(queryText = name)

    override fun getAllAirport(): Flow<List<Airport>> = flightDao.getAllAirport()

    override fun getAllFavorites(): Flow<List<Favorite>> = flightDao.getAllFavorites()

    override suspend fun insertIntoFavorite(favorite: Favorite) = flightDao.insertIntoFavorite(favorite)

    override suspend fun deleteFromFavorite(favorite: Favorite) = flightDao.deleteFromFavorite(favorite)
}