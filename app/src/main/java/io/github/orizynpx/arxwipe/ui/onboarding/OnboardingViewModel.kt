package io.github.orizynpx.arxwipe.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.orizynpx.arxwipe.domain.model.MainField
import io.github.orizynpx.arxwipe.domain.model.PaperCategory
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class OnboardingUiState(
    val availableCategories: List<PaperCategory> = emptyList(),
    val filteredCategories: List<PaperCategory> = emptyList(),
    val selectedMajorFields: Set<MainField> = emptySet(),
    val selectedCategoryIds: Set<String> = emptySet(),
    val batchSize: Int = 20,
    val isLoading: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val paperRepository: PaperRepository
) : ViewModel() {

    private val _selectedMajorFields = MutableStateFlow<Set<MainField>>(emptySet())
    val selectedMainFields = _selectedMajorFields.asStateFlow()

    private val _selectedCategoryIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories = _selectedCategoryIds.asStateFlow()

    private val _batchSize = MutableStateFlow(20)
    val batchSize = _batchSize.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    private val _availableCategories = MutableStateFlow<List<PaperCategory>>(emptyList())

    private val _onboardingCompleteEvent = Channel<Unit>()
    val onboardingCompleteEvent = _onboardingCompleteEvent.receiveAsFlow()

    val filteredCategories: StateFlow<List<PaperCategory>> = combine(
        _availableCategories,
        _selectedMajorFields
    ) { categories, fields ->
        if (fields.isEmpty()) categories else categories.filter { it.group in fields }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<OnboardingUiState> = combine(
        _availableCategories,
        filteredCategories,
        _selectedMajorFields,
        _selectedCategoryIds,
        _batchSize,
        _isLoading
    ) { flows ->
        OnboardingUiState(
            availableCategories = flows[0] as List<PaperCategory>,
            filteredCategories = flows[1] as List<PaperCategory>,
            selectedMajorFields = flows[2] as Set<MainField>,
            selectedCategoryIds = flows[3] as Set<String>,
            batchSize = flows[4] as Int,
            isLoading = flows[5] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OnboardingUiState()
    )

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            _availableCategories.value = paperRepository.getAvailableCategories()
            _isLoading.value = false
        }
    }

    fun toggleMainField(field: MainField) {
        _selectedMajorFields.update { current ->
            if (current.contains(field)) current - field else current + field
        }
    }

    
    fun toggleMajorField(field: MainField) = toggleMainField(field)

    fun toggleCategory(categoryId: String) {
        _selectedCategoryIds.update { current ->
            if (current.contains(categoryId)) current - categoryId else current + categoryId
        }
    }

    fun toggleFieldWithCategories(field: MainField, isChecked: Boolean) {
        val fieldCategoryIds = _availableCategories.value
            .filter { it.group == field }
            .map { it.categoryId }
            .toSet()

        _selectedCategoryIds.update { current ->
            if (isChecked) current + fieldCategoryIds else current - fieldCategoryIds
        }
    }

    fun setBatchSize(size: Int) {
        _batchSize.value = size
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            Timber.d("Completing onboarding with categories: ${_selectedCategoryIds.value}")
            paperRepository.saveOnboardingPreferences(_selectedCategoryIds.value.toList(), _batchSize.value)
            _onboardingCompleteEvent.send(Unit)
        }
    }

    
    fun completeOnboarding(selectedCategoryIds: List<String>, batchSize: Int) {
        viewModelScope.launch {
            Timber.d("Completing onboarding (overload) with categories: $selectedCategoryIds")
            paperRepository.saveOnboardingPreferences(selectedCategoryIds, batchSize)
            _onboardingCompleteEvent.send(Unit)
        }
    }
}
