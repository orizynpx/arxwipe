package io.github.orizynpx.arxwipe.domain.usecase

import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.Triage
import io.github.orizynpx.arxwipe.domain.repository.InteractionRepository
import io.github.orizynpx.arxwipe.domain.repository.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.uuid.Uuid

class GetTriageDeckUseCaseTest {

    private lateinit var interactionRepository: InteractionRepository
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var getTriageDeckUseCase: GetTriageDeckUseCase

    @Before
    fun setUp() {
        interactionRepository = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)
        getTriageDeckUseCase = GetTriageDeckUseCase(
            interactionRepository,
            preferencesRepository
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `when active triage exists and not all papers swiped, return current deck`() = runTest {
        
        val papers = listOf(mockk<ArxivPaper>(), mockk<ArxivPaper>())
        val activeTriage = Triage(Uuid.random(), papers)
        val currentSwipedIndex = 1 

        coEvery { interactionRepository.getActiveTriage() } returns activeTriage
        every { preferencesRepository.getCurrentTriageIndex() } returns flowOf(currentSwipedIndex)

        
        val result = getTriageDeckUseCase()

        
        assert(result == activeTriage)
        coVerify(exactly = 1) { interactionRepository.getActiveTriage() }
    }

    @Test
    fun `when active triage is fully swiped, return null`() = runTest {
        
        val oldPapers = listOf(mockk<ArxivPaper>())
        val activeTriage = Triage(Uuid.random(), oldPapers)
        val currentSwipedIndex = 1 

        coEvery { interactionRepository.getActiveTriage() } returns activeTriage
        every { preferencesRepository.getCurrentTriageIndex() } returns flowOf(currentSwipedIndex)

        
        val result = getTriageDeckUseCase()

        
        assert(result == null)
    }
}
