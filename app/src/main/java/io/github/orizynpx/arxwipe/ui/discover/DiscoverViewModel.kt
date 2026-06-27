package io.github.orizynpx.arxwipe.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.SwipeType
import io.github.orizynpx.arxwipe.domain.repository.CollectionRepository
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import io.github.orizynpx.arxwipe.domain.repository.PreferencesRepository
import io.github.orizynpx.arxwipe.domain.usecase.CompileNewTriageUseCase
import io.github.orizynpx.arxwipe.domain.usecase.SwipePaperUseCase
import io.github.orizynpx.arxwipe.domain.usecase.UndoSwipeUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface DiscoverUiState {
    data object Loading : DiscoverUiState
    data class Success(
        val papers: List<ArxivPaper>,
        val currentIndex: Int,
        val progressPercentage: Int,
        val isFallback: Boolean = false
    ) : DiscoverUiState
    data class Error(val message: String, val type: ErrorType = ErrorType.GENERAL) : DiscoverUiState
    data object Exhausted : DiscoverUiState

    enum class ErrorType {
        NETWORK,
        EMPTY_RESULT,
        NO_PAPERS_AVAILABLE,
        GENERAL
    }
}

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val swipePaperUseCase: SwipePaperUseCase,
    private val undoSwipeUseCase: UndoSwipeUseCase,
    private val compileNewTriageUseCase: CompileNewTriageUseCase,
    private val paperRepository: PaperRepository,
    private val preferencesRepository: PreferencesRepository,
    private val collectionRepository: CollectionRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    private val _isCompiling = MutableStateFlow(false)

    private val workManager = WorkManager.getInstance(context)
    private val isWorkRunning = workManager.getWorkInfosByTagFlow("TriageSync")
        .map { workInfos ->
            workInfos.any { it.state == WorkInfo.State.RUNNING }
        }
        .distinctUntilChanged()

    val triagePapersFlow: Flow<List<ArxivPaper>> = paperRepository.getActiveTriagePapers()
    val collectionsFlow = collectionRepository.getUserCollections()

    val uiState: StateFlow<DiscoverUiState> = combine(
        paperRepository.getActiveTriagePapers(),
        preferencesRepository.getCurrentTriageIndex(),
        preferencesRepository.getOnboardingPreferences(),
        _error,
        _isCompiling,
        isWorkRunning
    ) { flowArray ->
        val papers = flowArray[0] as List<ArxivPaper>
        val index = flowArray[1] as Int
        val prefs = flowArray[2] as io.github.orizynpx.arxwipe.domain.model.OnboardingPrefs
        val error = flowArray[3] as String?
        val compiling = flowArray[4] as Boolean
        val backgroundRunning = flowArray[5] as Boolean

        val isLoading = compiling || backgroundRunning
        
        when {
            
            error != null && papers.isEmpty() -> {
                val type = when {
                    error.contains("network", ignoreCase = true) || error.contains("host", ignoreCase = true) -> DiscoverUiState.ErrorType.NETWORK
                    error.contains("no new papers", ignoreCase = true) -> DiscoverUiState.ErrorType.EMPTY_RESULT
                    else -> DiscoverUiState.ErrorType.GENERAL
                }
                DiscoverUiState.Error(error, type)
            }
            
            papers.isNotEmpty() && index < papers.size -> {
                val progress = ((index.toFloat() / papers.size) * 100).toInt()
                
                
                
                val selectedIds = prefs.selectedCategoryIds
                val isFallback = if (selectedIds.isEmpty()) false else {
                    papers.none { paper ->
                        selectedIds.any { selectedId ->
                            val prefix = selectedId.removeSuffix(".*")
                            paper.allCategories.any { it.categoryId.startsWith(prefix) }
                        }
                    }
                }

                DiscoverUiState.Success(papers, index, progress, isFallback)
            }
            
            isLoading -> DiscoverUiState.Loading
            
            papers.isEmpty() -> {
                
                
                if (error != null) {
                    DiscoverUiState.Error(error, DiscoverUiState.ErrorType.GENERAL)
                } else {
                    DiscoverUiState.Error(
                        "No papers found even in general feed. Please try again later.", 
                        DiscoverUiState.ErrorType.NO_PAPERS_AVAILABLE
                    )
                }
            }
            index >= papers.size -> DiscoverUiState.Exhausted
            else -> DiscoverUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DiscoverUiState.Loading
    )

    init {
        
        
        viewModelScope.launch {
            try {
                compileNewTriageUseCase(force = false)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    
    fun refreshTriage(force: Boolean) {
        viewModelScope.launch {
            _error.value = null
            _isCompiling.value = true
            try {
                compileNewTriageUseCase(force)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to compile triage"
            } finally {
                _isCompiling.value = false
            }
        }
    }

    fun swipePaper(paperId: String, type: SwipeType) {
        viewModelScope.launch {
            try {
                swipePaperUseCase(paperId, type)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to record swipe"
            }
        }
    }

    fun undoLastSwipe() {
        viewModelScope.launch {
            try {
                undoSwipeUseCase()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to undo swipe"
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun updatePaperCollection(paperId: String, collectionId: Uuid, isChecked: Boolean) {
        viewModelScope.launch {
            if (isChecked) {
                collectionRepository.addPaperToCollection(paperId, collectionId)
            } else {
                collectionRepository.removePaperFromCollection(paperId, collectionId)
            }
        }
    }
}
