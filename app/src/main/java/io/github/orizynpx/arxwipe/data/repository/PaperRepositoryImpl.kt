package io.github.orizynpx.arxwipe.data.repository

import io.github.orizynpx.arxwipe.data.local.dao.ArxwipeDao
import io.github.orizynpx.arxwipe.data.local.entity.toDomain
import io.github.orizynpx.arxwipe.data.local.entity.toEntity
import io.github.orizynpx.arxwipe.data.remote.ArxivApiService
import io.github.orizynpx.arxwipe.data.remote.dto.toDomain
import io.github.orizynpx.arxwipe.data.remote.parser.ArxivXmlParser
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.MainField
import io.github.orizynpx.arxwipe.domain.model.PaperCategory
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PaperRepositoryImpl @Inject constructor(
    private val dao: ArxwipeDao,
    private val api: ArxivApiService
) : PaperRepository {

    override suspend fun getDiscoveryFeed(categoryId: String?, limit: Int): List<ArxivPaper> {
        val query = categoryId?.let { "cat:$it" } ?: "all"
        return try {
            val responseBody = api.getPapers(searchQuery = query, maxResults = limit)
            val parsedEntries = ArxivXmlParser.parse(responseBody.byteStream())
            val papers = parsedEntries.map { it.toDomain() }

            // Cache immediately to the local database
            dao.insertPapers(papers.map { it.toEntity() })
            papers
        } catch (e: Exception) {
            // Failure fallback: read directly from simplified cache
            dao.getAllCachedPapers(limit).map { it.toDomain() }
        }
    }

    override suspend fun getPaperById(paperId: String): ArxivPaper? {
        return dao.getPaperById(paperId)?.toDomain()
    }

    override suspend fun getAvailableCategories(): List<PaperCategory> {
        return listOf(
            PaperCategory("cs.AI", "Artificial Intelligence", MainField.COMPUTER_SCIENCE, "AI"),
            PaperCategory("cs.LG", "Machine Learning", MainField.COMPUTER_SCIENCE, "ML"),
            PaperCategory("cs.CV", "Computer Vision", MainField.COMPUTER_SCIENCE, "CV"),
            PaperCategory("cs.CL", "Natural Language Processing", MainField.COMPUTER_SCIENCE, "NLP")
        )
    }

    // Bind onboarding configuration values
    override suspend fun saveOnboardingPreferences(categoryIds: List<String>, batchSize: Int) {
        // Implementation can save directly into Android DataStore standard library
    }

    override fun getOnboardingPreferences(): Flow<io.github.orizynpx.arxwipe.domain.model.OnboardingPrefs> {
        TODO("Not yet implemented")
    }
}