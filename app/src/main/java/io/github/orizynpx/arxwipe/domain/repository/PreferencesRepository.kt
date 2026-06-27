package io.github.orizynpx.arxwipe.domain.repository

import io.github.orizynpx.arxwipe.domain.model.OnboardingPrefs
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun isOnboardingComplete(): Flow<Boolean>
    fun getSelectedCategories(): Flow<Set<String>>
    fun getBatchSize(): Flow<Int>

    fun getOnboardingPreferences(): Flow<OnboardingPrefs>
    suspend fun saveOnboardingPreferences(categoryIds: List<String>, batchSize: Int)

    fun getCurrentTriageIndex(): Flow<Int>
    suspend fun saveTriageIndex(index: Int)

    fun getLastFetchTime(): Flow<Long>
    suspend fun saveLastFetchTime(time: Long)

    
    fun getLastFetchDate(): Flow<String>
    suspend fun saveLastFetchDate(date: String)

    
    fun getLastTriageDate(): Flow<String>
    suspend fun saveLastTriageDate(date: String)

    fun isDynamicColorsEnabled(): Flow<Boolean>
    suspend fun saveDynamicColorsPreference(enabled: Boolean)

    suspend fun syncWithRemote()
    suspend fun clearPreferences()
}