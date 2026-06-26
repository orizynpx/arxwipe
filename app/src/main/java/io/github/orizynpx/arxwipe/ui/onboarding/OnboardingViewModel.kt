package io.github.orizynpx.arxwipe.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.orizynpx.arxwipe.domain.model.PaperCategory
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val paperRepository: PaperRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<PaperCategory>>(emptyList())
    val categories: StateFlow<List<PaperCategory>> = _categories.asStateFlow()

    init {
        viewModelScope.launch {
            _categories.value = paperRepository.getAvailableCategories()
        }
    }

    fun savePreferences(selectedIds: List<String>, batchSize: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            paperRepository.saveOnboardingPreferences(selectedIds, batchSize)
            onComplete()
        }
    }
}