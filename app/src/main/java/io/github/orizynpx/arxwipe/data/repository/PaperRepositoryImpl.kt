package io.github.orizynpx.arxwipe.data.repository

import io.github.orizynpx.arxwipe.data.local.dao.PaperDao
import io.github.orizynpx.arxwipe.data.local.entity.AuthorEntity
import io.github.orizynpx.arxwipe.data.local.entity.PaperAuthorCrossRef
import io.github.orizynpx.arxwipe.data.local.entity.toDomain
import io.github.orizynpx.arxwipe.data.local.entity.toEntity
import io.github.orizynpx.arxwipe.data.remote.ArxivApiService
import io.github.orizynpx.arxwipe.data.remote.dto.ArxivFeedDto
import io.github.orizynpx.arxwipe.data.remote.mapper.toDomain
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.ArxivTaxonomy
import io.github.orizynpx.arxwipe.domain.model.PaperCategory
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import io.github.orizynpx.arxwipe.domain.repository.PreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class PaperRepositoryImpl @Inject constructor(
    private val paperDao: PaperDao,
    private val apiService: ArxivApiService,
    private val xml: XML,
    private val preferencesRepository: PreferencesRepository
) : PaperRepository {

    override suspend fun getDiscoveryFeed(
        categoryId: String?,
        limit: Int,
        forceRefresh: Boolean
    ): List<ArxivPaper> {
        val lastFetch = preferencesRepository.getLastFetchTime().first()
        val currentTime = System.currentTimeMillis()
        val twentyFourHoursInMillis = 86_400_000L

        return if (forceRefresh || (currentTime - lastFetch >= twentyFourHoursInMillis)) {
            val searchQuery = categoryId?.let { 
                if (it.endsWith("*")) "cat:${it.replace("*", "")}*" else "cat:$it"
            } ?: "all"
            try {
                val responseBody = withRetry {
                    apiService.getPapers(
                        searchQuery = searchQuery,
                        maxResults = 100,
                        sortBy = "submittedDate",
                        sortOrder = "descending"
                    )
                }
                val xmlString = responseBody.string()
                val feed = xml.decodeFromString<ArxivFeedDto>(xmlString)
                val papers = feed.entries.map { it.toDomain() }

                cachePapers(papers)

                preferencesRepository.saveLastFetchTime(currentTime)
                papers
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch discovery feed for category: $categoryId")
                fetchFromLocal(categoryId, limit)
            }
        } else {
            Timber.d("24 hours have not elapsed. Skipping network request and reading from local cache.")
            fetchFromLocal(categoryId, limit)
        }
    }

    override suspend fun getCachedPapers(categoryId: String?, limit: Int): List<ArxivPaper> {
        return fetchFromLocal(categoryId, limit)
    }

    override suspend fun fetchAndCachePapers(categoryId: String?, targetCount: Int): List<ArxivPaper> {
        val searchQuery = categoryId?.let { 
            if (it.endsWith("*")) "cat:${it.replace("*", "")}*" else "cat:$it"
        } ?: "all"
        
        val accumulated = LinkedHashMap<String, ArxivPaper>()
        var start = 0
        try {
            while (accumulated.size < targetCount && start < MAX_PAGINATION_RESULTS) {
                val responseBody = withRetry {
                    apiService.getPapers(
                        searchQuery = searchQuery,
                        maxResults = PAGE_SIZE,
                        start = start,
                        sortBy = "submittedDate",
                        sortOrder = "descending"
                    )
                }
                val xmlString = responseBody.string()
                val feed = xml.decodeFromString<ArxivFeedDto>(xmlString)
                val papers = feed.entries.map { it.toDomain() }
                if (papers.isEmpty()) break

                cachePapers(papers)
                papers.forEach { accumulated[it.arxivId] = it }
                Timber.d("Paginated fetch category=$categoryId start=$start got=${papers.size} total=${accumulated.size}")
                start += PAGE_SIZE
            }
            if (accumulated.isNotEmpty()) {
                preferencesRepository.saveLastFetchTime(System.currentTimeMillis())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Paginated fetch failed for category=$categoryId at start=$start")
        }
        return accumulated.values.toList()
    }

    
    private suspend fun cachePapers(papers: List<ArxivPaper>) {
        papers.forEach { paper ->
            val paperEntity = paper.toEntity()
            val authors = paper.authors.map { AuthorEntity(it.authorId.toString(), it.name) }
            val crossRefs = paper.authors.map { PaperAuthorCrossRef(paper.arxivId, it.authorId.toString()) }
            paperDao.insertPaperWithAuthors(paperEntity, authors, crossRefs)
        }
    }

    private suspend fun fetchFromLocal(categoryId: String?, limit: Int): List<ArxivPaper> {
        return if (categoryId != null) {
            val pattern = if (categoryId.endsWith("*")) {
                categoryId.replace("*", "%")
            } else {
                categoryId
            }
            paperDao.getPapersByCategory(pattern, limit).map { it.toDomain() }
        } else {
            paperDao.getAllPapers(limit).map { it.toDomain() }
        }
    }

    override suspend fun getPaperById(paperId: String): ArxivPaper? {
        return paperDao.getPaperWithAuthorsById(paperId)?.toDomain()
    }

    override suspend fun getAvailableCategories(): List<PaperCategory> {
        return ArxivTaxonomy.categories
    }

    override suspend fun searchPapers(
        query: String,
        categoryIds: List<String>,
        start: Int
    ): List<ArxivPaper> {
        
        
        val trimmedQuery = query.trim()
        val catClause = categoryIds.filter { it.isNotBlank() }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = " OR ") { "cat:$it" }
            ?.let { "($it)" }
        val termClause = trimmedQuery.takeIf { it.isNotEmpty() }?.let { "ti:$it" }
        
        val searchQuery = listOfNotNull(catClause, termClause)
            .joinToString(separator = " AND ")
            .ifBlank { "all" }

        val responseBody = withRetry {
            apiService.getPapers(
                searchQuery = searchQuery,
                maxResults = SEARCH_PAGE_SIZE,
                start = start,
                
                sortBy = "lastUpdatedDate",
                sortOrder = "descending"
            )
        }
        val xmlString = responseBody.string()
        val feed = xml.decodeFromString<ArxivFeedDto>(xmlString)
        val papers = feed.entries.map { it.toDomain() }
        
        cachePapers(papers)
        return papers
    }

    override fun getActiveTriagePapers(): Flow<List<ArxivPaper>> {
        return paperDao.getActivePapers().map { papers ->
            papers.map { it.toDomain() }
        }
    }

    override suspend fun saveOnboardingPreferences(selectedCategoryIds: List<String>, batchSize: Int) {
        preferencesRepository.saveOnboardingPreferences(selectedCategoryIds, batchSize)
    }

    override suspend fun fetchAndSavePaper(paperId: String): ArxivPaper? {
        return try {
            val responseBody = withRetry {
                apiService.getPapers(
                    searchQuery = "id:$paperId",
                    maxResults = 1,
                    sortBy = "submittedDate",
                    sortOrder = "descending"
                )
            }
            val xmlString = responseBody.string()
            val feed = xml.decodeFromString<ArxivFeedDto>(xmlString)
            val paper = feed.entries.firstOrNull()?.toDomain()
            paper?.let { cachePapers(listOf(it)) }
            paper
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch and save paper: $paperId")
            null
        }
    }

    private suspend fun <T> withRetry(
        times: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 5000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(times - 1) { attempt ->
            try {
                return block()
            } catch (e: IOException) {
                Timber.w(e, "Retryable error occurred on attempt ${attempt + 1}. Retrying in ${currentDelay}ms...")
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
        return block() 
    }

    private companion object {
        const val PAGE_SIZE = 50
        
        const val MAX_PAGINATION_RESULTS = 250
        
        const val SEARCH_PAGE_SIZE = 50
    }
}