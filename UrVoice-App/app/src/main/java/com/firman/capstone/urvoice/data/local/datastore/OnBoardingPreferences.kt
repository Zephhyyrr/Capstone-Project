package com.firman.capstone.urvoice.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_preferences")
class OnboardingPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }

    val hasSeenOnboarding: Flow<Boolean> = context.onboardingDataStore.data.map { preferences ->
        preferences[HAS_SEEN_ONBOARDING] == true
    }

    suspend fun setOnboardingStatus(hasSeenOnboarding: Boolean) {
        context.onboardingDataStore.edit { preferences ->
            preferences[HAS_SEEN_ONBOARDING] = hasSeenOnboarding
        }
    }
}