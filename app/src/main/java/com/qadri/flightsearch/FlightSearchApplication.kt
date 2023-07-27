package com.qadri.flightsearch

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.qadri.flightsearch.data.AppContainer
import com.qadri.flightsearch.data.AppDataContainer
import com.qadri.flightsearch.data.UserPreferenceRepository

private const val SEARCHED_HISTORY_PREFERENCE_NAME = "searched_history_preference"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = SEARCHED_HISTORY_PREFERENCE_NAME
)
class FlightSearchApplication: Application() {
    lateinit var container: AppContainer

    lateinit var userPreferenceRepository: UserPreferenceRepository
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        userPreferenceRepository = UserPreferenceRepository(dataStore)
    }
}