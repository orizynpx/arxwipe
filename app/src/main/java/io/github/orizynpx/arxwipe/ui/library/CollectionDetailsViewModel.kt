package io.github.orizynpx.arxwipe.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.PaperCollection
import io.github.orizynpx.arxwipe.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.Uuid

enum class SortOrder {
    LATEST,
    TITLE_AZ
}

@HiltViewModel
class CollectionDetailsViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val _collectionId = MutableStateFlow<Uuid?>(null)
    val collectionId: Uuid? get() = _collectionId.value
    
    private val _sortOrder = MutableStateFlow(SortOrder.LATEST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    val papers: StateFlow<List<ArxivPaper>> = combine(
        collectionRepository.getUserCollections(),
        _collectionId,
        _sortOrder
    ) { collections, id, sort ->
        val collection = collections.find { it.collectionId == id }
        val list = collection?.papers ?: emptyList()

        when (sort) {
            SortOrder.LATEST -> list.sortedByDescending { it.publishedAt }
            SortOrder.TITLE_AZ -> list.sortedBy { it.title }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val collectionName: StateFlow<String> = combine(
        collectionRepository.getUserCollections(),
        _collectionId
    ) { collections, id ->
        collections.find { it.collectionId == id }?.name ?: ""
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val collections: StateFlow<List<PaperCollection>> = collectionRepository.getUserCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun loadCollection(collectionId: Uuid, sortBy: SortOrder) {
        _collectionId.value = collectionId
        _sortOrder.value = sortBy
    }

    fun addPaper(paperId: String, collectionId: Uuid) {
        viewModelScope.launch {
            collectionRepository.addPaperToCollection(paperId, collectionId)
        }
    }

    fun removePaper(paperId: String, collectionId: Uuid) {
        viewModelScope.launch {
            collectionRepository.removePaperFromCollection(paperId, collectionId)
        }
    }
}
