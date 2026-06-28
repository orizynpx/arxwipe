package io.github.orizynpx.arxwipe.ui.paperdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.PaperCollection
import io.github.orizynpx.arxwipe.domain.model.SwipeType
import io.github.orizynpx.arxwipe.domain.repository.CollectionRepository
import io.github.orizynpx.arxwipe.domain.repository.InteractionRepository
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import io.github.orizynpx.arxwipe.domain.usecase.SwipePaperUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface PaperDetailsUiState {
    data object Loading : PaperDetailsUiState
    data class Success(
        val paper: ArxivPaper,
        val isSaved: Boolean,
        val collections: List<PaperCollection>
    ) : PaperDetailsUiState
    data class Error(val message: String) : PaperDetailsUiState
}

@HiltViewModel
class PaperDetailsViewModel @Inject constructor(
    private val paperRepository: PaperRepository,
    private val interactionRepository: InteractionRepository,
    private val collectionRepository: CollectionRepository,
    private val swipePaperUseCase: SwipePaperUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaperDetailsUiState>(PaperDetailsUiState.Loading)
    val uiState: StateFlow<PaperDetailsUiState> = _uiState.asStateFlow()

    fun loadPaper(arxivId: String) {
        viewModelScope.launch {
            _uiState.value = PaperDetailsUiState.Loading
            try {
                val paper = paperRepository.getPaperById(arxivId)
                if (paper != null) {
                    interactionRepository.getSwipedPaperIdsFlow().map { it.contains(arxivId) }
                        .combine(collectionRepository.getUserCollections()) { isSaved, collections ->
                            PaperDetailsUiState.Success(
                                paper = paper,
                                isSaved = isSaved,
                                collections = collections
                            )
                        }.collect { state ->
                            _uiState.value = state
                        }
                } else {
                    _uiState.value = PaperDetailsUiState.Error(" Paper not found")
                }
            } catch (e: Exception) {
                _uiState.value = PaperDetailsUiState.Error(e.message ?: "Failed to load paper")
            }
        }
    }

    fun saveToReadLater(arxivId: String) {
        viewModelScope.launch {
            try {
                swipePaperUseCase(arxivId, SwipeType.SAVE)
            } catch (e: Exception) {
                
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
