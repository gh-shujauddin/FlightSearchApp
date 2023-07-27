package com.qadri.flightsearch.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferenceRepository (
    private val dataStore: DataStore<Preferences>
) {

    val searchedHistory: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preference ->
            preference[SEARCHED_HISTORY]
        }

    suspend fun saveSearchedHistory(searchedString: String) {
        dataStore.edit { preferences ->
            preferences[SEARCHED_HISTORY] = searchedString
        }
    }

    private companion object {
        val SEARCHED_HISTORY = stringPreferencesKey("searched_history")
        const val TAG = "UserPreferenceRepo"
    }
}