package io.github.orizynpx.arxwipe.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.SwipeType
import io.github.orizynpx.arxwipe.domain.usecase.GetTriageDeckUseCase
import io.github.orizynpx.arxwipe.domain.usecase.SwipePaperUseCase
import io.github.orizynpx.arxwipe.domain.usecase.UndoSwipeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DiscoverUiState {
    object Loading : DiscoverUiState
    data class Success(val papers: List<ArxivPaper>, val currentIndex: Int) : DiscoverUiState
    object Exhausted : DiscoverUiState
    data class Error(val message: String) : DiscoverUiState
}

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val getTriageDeckUseCase: GetTriageDeckUseCase,
    private val swipePaperUseCase: SwipePaperUseCase,
    private val undoSwipeUseCase: UndoSwipeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Loading)
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private var currentIndex = 0
    private var cachedPapersList: List<ArxivPaper> = emptyList()

    init {
        loadDeck()
    }

    fun loadDeck() {
        _uiState.value = DiscoverUiState.Loading
        viewModelScope.launch {
            try {
                val triage = getTriageDeckUseCase()
                cachedPapersList = triage.papers
                if (cachedPapersList.isEmpty()) {
                    _uiState.value = DiscoverUiState.Exhausted
                } else {
                    currentIndex = 0
                    _uiState.value = DiscoverUiState.Success(cachedPapersList, currentIndex)
                }
            } catch (e: Exception) {
                _uiState.value = DiscoverUiState.Error(e.message ?: "Failed to load papers")
            }
        }
    }

    fun swipe(directionRight: Boolean) {
        if (currentIndex >= cachedPapersList.size) return
        val paper = cachedPapersList[currentIndex]
        val type = if (directionRight) SwipeType.SAVE else SwipeType.DISMISS

        viewModelScope.launch {
            swipePaperUseCase(paper.arxivId, type)
            currentIndex++
            if (currentIndex >= cachedPapersList.size) {
                _uiState.value = DiscoverUiState.Exhausted
            } else {
                _uiState.value = DiscoverUiState.Success(cachedPapersList, currentIndex)
            }
        }
    }

    fun undo() {
        viewModelScope.launch {
            undoSwipeUseCase()
            if (currentIndex > 0) {
                currentIndex--
                _uiState.value = DiscoverUiState.Success(cachedPapersList, currentIndex)
            }
        }
    }
}