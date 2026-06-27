package io.github.orizynpx.arxwipe.domain.repository

import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.PaperCategory
import kotlinx.coroutines.flow.Flow

interface PaperRepository {
    suspend fun getDiscoveryFeed(
        categoryId: String?,
        limit: Int = 20,
        forceRefresh: Boolean = false
    ): List<ArxivPaper>

    suspend fun getCachedPapers(categoryId: String?, limit: Int): List<ArxivPaper>

    
    suspend fun fetchAndCachePapers(categoryId: String?, targetCount: Int): List<ArxivPaper>

    suspend fun getPaperById(paperId: String): ArxivPaper?
    suspend fun getAvailableCategories(): List<PaperCategory>
    
    suspend fun searchPapers(
        query: String,
        categoryIds: List<String>,
        start: Int = 0
    ): List<ArxivPaper>

    fun getActiveTriagePapers(): Flow<List<ArxivPaper>>

    suspend fun saveOnboardingPreferences(selectedCategoryIds: List<String>, batchSize: Int)
}