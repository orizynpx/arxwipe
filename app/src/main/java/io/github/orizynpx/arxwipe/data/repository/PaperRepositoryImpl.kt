package io.github.orizynpx.arxwipe.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.orizynpx.arxwipe.data.local.dao.ArxwipeDao
import io.github.orizynpx.arxwipe.data.local.entity.toDomain
import io.github.orizynpx.arxwipe.data.local.entity.toEntity
import io.github.orizynpx.arxwipe.data.remote.ArxivApiService
import io.github.orizynpx.arxwipe.data.remote.dto.toDomain
import io.github.orizynpx.arxwipe.data.remote.parser.ArxivXmlParser
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.MainField
import io.github.orizynpx.arxwipe.domain.model.OnboardingPrefs
import io.github.orizynpx.arxwipe.domain.model.PaperCategory
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaperRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: ArxwipeDao,
    private val api: ArxivApiService
) : PaperRepository {

    private val prefs = context.getSharedPreferences("arxwipe_prefs", Context.MODE_PRIVATE)
    private val _onboardingFlow = MutableStateFlow(loadPrefs())

    private fun loadPrefs(): OnboardingPrefs {
        val categories = prefs.getStringSet("selected_categories", emptySet())?.toList() ?: emptyList()
        val batchSize = prefs.getInt("batch_size", 20)
        return OnboardingPrefs(categories, batchSize)
    }

    override suspend fun getDiscoveryFeed(categoryId: String?, limit: Int): List<ArxivPaper> {
        val query = categoryId?.let { "cat:$it" } ?: "all"
        return try {
            val responseBody = api.getPapers(searchQuery = query, maxResults = limit)
            val parsedEntries = ArxivXmlParser.parse(responseBody.byteStream())
            val papers = parsedEntries.map { it.toDomain() }

            dao.insertPapers(papers.map { it.toEntity() })
            papers
        } catch (e: Exception) {
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
            PaperCategory("cs.CL", "Natural Language Processing", MainField.COMPUTER_SCIENCE, "NLP"),
            PaperCategory("stat.ML", "Statistics - Machine Learning", MainField.STATISTICS, "Stat ML"),
            PaperCategory("math.ST", "Mathematical Statistics", MainField.MATHEMATICS, "Math Stat")
        )
    }

    override suspend fun saveOnboardingPreferences(categoryIds: List<String>, batchSize: Int) {
        prefs.edit()
            .putStringSet("selected_categories", categoryIds.toSet())
            .putInt("batch_size", batchSize)
            .apply()
        _onboardingFlow.value = OnboardingPrefs(categoryIds, batchSize)
    }

    override fun getOnboardingPreferences(): Flow<OnboardingPrefs> {
        return _onboardingFlow.asStateFlow()
    }
}