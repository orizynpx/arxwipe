package io.github.orizynpx.arxwipe.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.orizynpx.arxwipe.domain.model.OnboardingPrefs
import io.github.orizynpx.arxwipe.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    private object PreferencesKeys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val SELECTED_CATEGORIES = stringSetPreferencesKey("selected_categories")
        val BATCH_SIZE = intPreferencesKey("batch_size")
        val LAST_FETCH_TIME = longPreferencesKey("last_fetch_time")
        val LAST_FETCH_DATE = stringPreferencesKey("last_fetch_date")
        val LAST_TRIAGE_DATE = stringPreferencesKey("last_triage_date")
        val LAST_TRIAGE_CONFIG_CATEGORIES = stringSetPreferencesKey("last_triage_config_categories")
        val LAST_TRIAGE_CONFIG_BATCH_SIZE = intPreferencesKey("last_triage_config_batch_size")
        val CURRENT_TRIAGE_INDEX = intPreferencesKey("current_triage_index")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
    }

    override fun isOnboardingComplete(): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            val isComplete = preferences[PreferencesKeys.ONBOARDING_COMPLETE] ?: false
            val hasCategories = (preferences[PreferencesKeys.SELECTED_CATEGORIES] ?: emptySet()).isNotEmpty()
            
            
            isComplete || hasCategories
        }

    override fun getSelectedCategories(): Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_CATEGORIES] ?: emptySet()
        }

    override fun getBatchSize(): Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BATCH_SIZE] ?: 20
        }

    override fun getLastFetchTime(): Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_FETCH_TIME] ?: 0L
        }

    override fun getLastFetchDate(): Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_FETCH_DATE] ?: ""
        }

    override fun getLastTriageDate(): Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_TRIAGE_DATE] ?: ""
        }

    override fun getCurrentTriageIndex(): Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CURRENT_TRIAGE_INDEX] ?: 0
        }

    override fun isDynamicColorsEnabled(): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLORS] ?: false
        }

    suspend fun setOnboardingComplete(complete: Boolean) {
        Timber.d("Setting onboarding complete: $complete")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETE] = complete
        }
    }

    suspend fun saveCategoryPreferences(categories: Set<String>) {
        Timber.d("Saving category preferences: $categories")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_CATEGORIES] = categories
        }
    }

    suspend fun saveBatchSize(size: Int) {
        Timber.d("Saving batch size: $size")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BATCH_SIZE] = size
        }
    }

    override suspend fun saveLastFetchTime(time: Long) {
        Timber.d("Saving last fetch time: $time")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_FETCH_TIME] = time
        }
    }

    override suspend fun saveLastFetchDate(date: String) {
        Timber.d("Saving last fetch date: $date")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_FETCH_DATE] = date
        }
    }

    override suspend fun saveLastTriageDate(date: String) {
        Timber.d("Saving last triage date: $date")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_TRIAGE_DATE] = date
        }
    }

    override fun getLastTriageConfigCategories(): Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_TRIAGE_CONFIG_CATEGORIES] ?: emptySet()
        }

    override suspend fun saveLastTriageConfigCategories(categories: Set<String>) {
        Timber.d("Saving last triage config categories: $categories")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_TRIAGE_CONFIG_CATEGORIES] = categories
        }
    }

    override fun getLastTriageConfigBatchSize(): Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_TRIAGE_CONFIG_BATCH_SIZE] ?: 0
        }

    override suspend fun saveLastTriageConfigBatchSize(size: Int) {
        Timber.d("Saving last triage config batch size: $size")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_TRIAGE_CONFIG_BATCH_SIZE] = size
        }
    }

    override suspend fun saveTriageIndex(index: Int) {
        Timber.d("Saving triage index: $index")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_TRIAGE_INDEX] = index
        }
    }

    override suspend fun saveDynamicColorsPreference(enabled: Boolean) {
        Timber.d("Saving dynamic colors preference: $enabled")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLORS] = enabled
        }
    }

    override fun getOnboardingPreferences(): Flow<OnboardingPrefs> {
        return combine(
            getSelectedCategories(),
            getBatchSize()
        ) { categories, size ->
            OnboardingPrefs(categories.toList(), size)
        }
    }

    override suspend fun saveOnboardingPreferences(categoryIds: List<String>, batchSize: Int) {
        saveCategoryPreferences(categoryIds.toSet())
        saveBatchSize(batchSize)
        setOnboardingComplete(true)
    }

    override suspend fun syncWithRemote() {
        
    }

    override suspend fun clearPreferences() {
        Timber.d("Clearing all preferences")
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}