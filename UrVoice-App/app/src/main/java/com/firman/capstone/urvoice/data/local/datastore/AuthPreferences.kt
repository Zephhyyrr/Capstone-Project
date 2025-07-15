package com.firman.capstone.urvoice.data.local.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "auth_preferences")

@Singleton
class AuthPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    suspend fun saveAuthToken(token: String?) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("CheckToken", "Entered saveAuthToken with token: $token")
                context.dataStore.edit { preferences ->
                    if (!token.isNullOrEmpty()) {
                        Log.d("CheckToken", "Auth Preferences Save Token: $token")
                        preferences[TOKEN_KEY] = token
                    } else {
                        Log.d("CheckToken", "Token is null or empty, removing from preferences")
                        preferences.remove(TOKEN_KEY)
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthPreferences", "Error saving token", e)
                throw e
            }
        }
    }

    suspend fun clearSession() {
        withContext(Dispatchers.IO) {
            try {
                context.dataStore.edit { preferences ->
                    preferences.remove(TOKEN_KEY)
                }
                Log.d("AuthPreferences", "Session cleared successfully")
            } catch (e: Exception) {
                Log.e("AuthPreferences", "Error clearing session", e)
                throw e
            }
        }
    }

    val authToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN_KEY]
        }

    suspend fun getAuthToken(): String? {
        return try {
            authToken.first()
        } catch (e: Exception) {
            Log.e("AuthPreferences", "Error getting token", e)
            null
        }
    }
}