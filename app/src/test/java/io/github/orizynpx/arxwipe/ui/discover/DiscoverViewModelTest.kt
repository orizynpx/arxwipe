package io.github.orizynpx.arxwipe.ui.discover

import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.OnboardingPrefs
import io.github.orizynpx.arxwipe.domain.model.Triage
import io.github.orizynpx.arxwipe.domain.repository.CollectionRepository
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import io.github.orizynpx.arxwipe.domain.repository.PreferencesRepository
import io.github.orizynpx.arxwipe.domain.usecase.SwipePaperUseCase
import io.github.orizynpx.arxwipe.domain.usecase.UndoSwipeUseCase
import io.github.orizynpx.arxwipe.domain.usecase.CompileNewTriageUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import android.content.Context

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoverViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val swipePaperUseCase: SwipePaperUseCase = mockk()
    private val undoSwipeUseCase: UndoSwipeUseCase = mockk()
    private val compileNewTriageUseCase: CompileNewTriageUseCase = mockk()
    private val paperRepository: PaperRepository = mockk()
    private val preferencesRepository: PreferencesRepository = mockk()
    private val collectionRepository: CollectionRepository = mockk()
    private val context: Context = mockk(relaxed = true)

    private val triagePapersFlow = MutableStateFlow<List<ArxivPaper>>(emptyList())
    private val currentTriageIndexFlow = MutableStateFlow(0)
    private val onboardingPrefsFlow = MutableStateFlow(OnboardingPrefs(emptyList(), 20))

    private lateinit var viewModel: DiscoverViewModel

    @Before
    fun setUp() {
        every { paperRepository.getActiveTriagePapers() } returns triagePapersFlow
        every { preferencesRepository.getCurrentTriageIndex() } returns currentTriageIndexFlow
        every { preferencesRepository.getOnboardingPreferences() } returns onboardingPrefsFlow
        every { collectionRepository.getUserCollections() } returns flowOf(emptyList())
        coEvery { compileNewTriageUseCase(any()) } returns mockk<Triage>()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `when initialized with papers, transitions from Loading to Success`() = runTest {
        triagePapersFlow.value = listOf(mockk(), mockk())
        
        viewModel = DiscoverViewModel(
            swipePaperUseCase,
            undoSwipeUseCase,
            compileNewTriageUseCase,
            paperRepository,
            preferencesRepository,
            collectionRepository,
            context
        )

        
        assertEquals(DiscoverUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DiscoverUiState.Success)
        assertEquals(0, (state as DiscoverUiState.Success).currentIndex)
    }

    @Test
    fun `when swipe reaches total paper size, transitions to Exhausted`() = runTest {
        triagePapersFlow.value = listOf(mockk(), mockk())
        
        viewModel = DiscoverViewModel(
            swipePaperUseCase,
            undoSwipeUseCase,
            compileNewTriageUseCase,
            paperRepository,
            preferencesRepository,
            collectionRepository,
            context
        )
        
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is DiscoverUiState.Success)

        
        currentTriageIndexFlow.value = 2 
        
        advanceUntilIdle()

        assertEquals(DiscoverUiState.Exhausted, viewModel.uiState.value)
    }
}
