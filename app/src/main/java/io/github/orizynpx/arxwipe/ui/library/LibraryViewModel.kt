package io.github.orizynpx.arxwipe.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.orizynpx.arxwipe.domain.model.PaperCollection
import io.github.orizynpx.arxwipe.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.Uuid

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    val collections: StateFlow<List<PaperCollection>> = collectionRepository.getUserCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createCollection(name: String) {
        viewModelScope.launch {
            collectionRepository.createCollection(name)
        }
    }

    fun updateCollection(id: Uuid, name: String) {
        viewModelScope.launch {
            collectionRepository.updateCollection(id, name)
        }
    }

    fun deleteCollection(id: Uuid) {
        viewModelScope.launch {
            collectionRepository.deleteCollection(id)
        }
    }
}
