package io.github.orizynpx.arxwipe.domain.repository

import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.OnboardingPrefs
import io.github.orizynpx.arxwipe.domain.model.PaperCategory
import kotlinx.coroutines.flow.Flow

interface PaperRepository {
    suspend fun getDiscoveryFeed(
        categoryId: String?,
        limit: Int = 20,
    ): List<ArxivPaper>

    suspend fun getPaperById(paperId: String): ArxivPaper?
    suspend fun getAvailableCategories(): List<PaperCategory>

    suspend fun saveOnboardingPreferences(categoryIds: List<String>, batchSize: Int)
    fun getOnboardingPreferences(): Flow<OnboardingPrefs>
}
