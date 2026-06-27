package io.github.orizynpx.arxwipe.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.PaperCollection
import io.github.orizynpx.arxwipe.domain.repository.CollectionRepository
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import io.github.orizynpx.arxwipe.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.uuid.Uuid

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val papers: List<ArxivPaper>) : SearchUiState
    data object Empty : SearchUiState
    data class Error(val message: String) : SearchUiState
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val paperRepository: PaperRepository,
    private val collectionRepository: CollectionRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    
    private val _isPaginating = MutableStateFlow(false)
    val isPaginating: StateFlow<Boolean> = _isPaginating.asStateFlow()

    val collections: StateFlow<List<PaperCollection>> = collectionRepository.getUserCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    
    val searchHistory: StateFlow<List<String>> = searchHistoryRepository.getHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var searchJob: Job? = null

    
    private var currentQuery: String = ""
    private var currentCategories: List<String> = emptyList()
    private val loadedPapers = mutableListOf<ArxivPaper>()
    private var nextStart = 0
    private var endReached = false
    private var lastRequestAt = 0L

    
    fun search(query: String, categories: List<String>) {
        searchJob?.cancel()
        currentQuery = query
        currentCategories = categories
        loadedPapers.clear()
        nextStart = 0
        endReached = false
        _isPaginating.value = false

        searchJob = viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val page = fetchPage(nextStart)
                loadedPapers.addAll(page)
                nextStart += page.size
                endReached = page.size < PAGE_SIZE
                _uiState.value = if (loadedPapers.isEmpty()) {
                    SearchUiState.Empty
                } else {
                    SearchUiState.Success(loadedPapers.toList())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Search failed")
            }
        }

        if (query.isNotBlank()) {
            viewModelScope.launch { searchHistoryRepository.record(query) }
        }
    }

    
    fun loadMore() {
        if (endReached || _isPaginating.value) return
        if (_uiState.value !is SearchUiState.Success) return

        _isPaginating.value = true
        viewModelScope.launch {
            try {
                val page = fetchPage(nextStart)
                val existingIds = loadedPapers.mapTo(HashSet()) { it.arxivId }
                val fresh = page.filter { it.arxivId !in existingIds }
                loadedPapers.addAll(fresh)
                nextStart += page.size
                endReached = page.size < PAGE_SIZE
                _uiState.value = SearchUiState.Success(loadedPapers.toList())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                
                Timber.e(e, "Failed to load more search results")
            } finally {
                _isPaginating.value = false
            }
        }
    }

    
    private suspend fun fetchPage(start: Int): List<ArxivPaper> {
        if (lastRequestAt != 0L) {
            val elapsed = System.currentTimeMillis() - lastRequestAt
            if (elapsed < MIN_REQUEST_INTERVAL_MS) {
                delay(MIN_REQUEST_INTERVAL_MS - elapsed)
            }
        }
        val page = paperRepository.searchPapers(currentQuery, currentCategories, start)
        lastRequestAt = System.currentTimeMillis()
        return page
    }

    fun deleteHistory(query: String) {
        viewModelScope.launch { searchHistoryRepository.delete(query) }
    }

    fun addPaperToCollection(paperId: String, collectionId: Uuid) {
        viewModelScope.launch {
            collectionRepository.addPaperToCollection(paperId, collectionId)
        }
    }

    fun removePaperFromCollection(paperId: String, collectionId: Uuid) {
        viewModelScope.launch {
            collectionRepository.removePaperFromCollection(paperId, collectionId)
        }
    }

    private companion object {
        const val PAGE_SIZE = 50
        const val MIN_REQUEST_INTERVAL_MS = 3_000L
    }
}
