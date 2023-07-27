package com.qadri.flightsearch.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {

    @Query("SELECT * FROM airport WHERE LOWER(iata_code) LIKE '%' || LOWER(:queryText) || '%' OR LOWER(name) LIKE '%' || LOWER(:queryText) || '%' ORDER BY passengers DESC")
    fun getItemFromAirport(queryText: String): Flow<List<Airport>>

    @Query("select * from airport")
    fun getAllAirport(): Flow<List<Airport>>

    @Query("SELECT * FROM favorite")
    fun getAllFavorites(): Flow<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIntoFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFromFavorite(favorite: Favorite)
}