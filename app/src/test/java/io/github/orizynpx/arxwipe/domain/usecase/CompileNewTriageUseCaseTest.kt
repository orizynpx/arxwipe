package io.github.orizynpx.arxwipe.domain.usecase

import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.OnboardingPrefs
import io.github.orizynpx.arxwipe.domain.model.Triage
import io.github.orizynpx.arxwipe.domain.repository.InteractionRepository
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import io.github.orizynpx.arxwipe.domain.repository.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CompileNewTriageUseCaseTest {

    private lateinit var interactionRepository: InteractionRepository
    private lateinit var paperRepository: PaperRepository
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var compileNewTriageUseCase: CompileNewTriageUseCase

    @Before
    fun setUp() {
        interactionRepository = mockk(relaxed = true)
        paperRepository = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)
        compileNewTriageUseCase = CompileNewTriageUseCase(
            interactionRepository,
            paperRepository,
            preferencesRepository
        )
        
        
        coEvery { preferencesRepository.getOnboardingPreferences() } returns flowOf(
            OnboardingPrefs(selectedCategoryIds = listOf("cs.AI"), batchSize = 10)
        )
        coEvery { preferencesRepository.getCurrentTriageIndex() } returns flowOf(0)
        coEvery { preferencesRepository.getLastTriageDate() } returns flowOf("")
        coEvery { preferencesRepository.getLastTriageConfigCategories() } returns flowOf(setOf("cs.AI"))
        coEvery { preferencesRepository.getLastTriageConfigBatchSize() } returns flowOf(10)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `when active triage exists and not finished, reuse it`() = runTest {
        
        val papers = List(10) { mockk<ArxivPaper>() }
        val activeTriage = Triage(Uuid.random(), papers)
        coEvery { interactionRepository.getActiveTriage() } returns activeTriage
        coEvery { preferencesRepository.getCurrentTriageIndex() } returns flowOf(5)

        
        val result = compileNewTriageUseCase(force = false)

        
        assertEquals(activeTriage, result)
        coVerify(exactly = 0) { interactionRepository.replaceTriage(any()) }
    }

    @Test
    fun `when active triage exhausted, compile new one from local`() = runTest {
        
        val activeTriage = Triage(Uuid.random(), List(5) { mockk<ArxivPaper>() })
        coEvery { interactionRepository.getActiveTriage() } returns activeTriage
        coEvery { preferencesRepository.getCurrentTriageIndex() } returns flowOf(5)
        
        val localPapers = List(30) { i ->
            mockk<ArxivPaper> {
                coEvery { arxivId } returns "id_$i"
                coEvery { allCategories } returns listOf(mockk { coEvery { categoryId } returns "cs.AI" })
            }
        }
        coEvery { paperRepository.getCachedPapers("cs.AI", any()) } returns localPapers
        coEvery { interactionRepository.getSwipedPaperIds() } returns emptySet()

        
        val result = compileNewTriageUseCase(force = false)

        
        assertEquals(10, result.papers.size)
        coVerify { interactionRepository.replaceTriage(any()) }
        coVerify(exactly = 0) { paperRepository.fetchAndCachePapers(any(), any()) }
    }

    @Test
    fun `when force is true, compile new one even if active triage exists`() = runTest {
        
        val activeTriage = Triage(Uuid.random(), List(10) { mockk<ArxivPaper>() })
        coEvery { interactionRepository.getActiveTriage() } returns activeTriage
        coEvery { preferencesRepository.getCurrentTriageIndex() } returns flowOf(0)
        
        val localPapers = List(20) { i ->
            mockk<ArxivPaper> {
                coEvery { arxivId } returns "id_$i"
                coEvery { allCategories } returns listOf(mockk { coEvery { categoryId } returns "cs.AI" })
            }
        }
        coEvery { paperRepository.getCachedPapers("cs.AI", any()) } returns localPapers

        
        val result = compileNewTriageUseCase(force = true)

        
        coVerify { interactionRepository.replaceTriage(any()) }
    }

    @Test
    fun `when local pool too small, fetch from remote`() = runTest {
        
        coEvery { interactionRepository.getActiveTriage() } returns null
        coEvery { paperRepository.getCachedPapers("cs.AI", any()) } returnsMany listOf(
            emptyList(), 
            List(20) { i -> 
                mockk<ArxivPaper> {
                    coEvery { arxivId } returns "id_$i"
                    coEvery { allCategories } returns listOf(mockk { coEvery { categoryId } returns "cs.AI" })
                }
            }
        )

        
        compileNewTriageUseCase()

        
        coVerify { paperRepository.fetchAndCachePapers("cs.AI", 100) }
        coVerify { interactionRepository.replaceTriage(any()) }
    }

    @Test
    fun `when preferred categories empty, fallback to general`() = runTest {
        
        coEvery { preferencesRepository.getOnboardingPreferences() } returns flowOf(
            OnboardingPrefs(selectedCategoryIds = emptyList(), batchSize = 5)
        )
        coEvery { interactionRepository.getActiveTriage() } returns null
        coEvery { interactionRepository.getSwipedPaperIds() } returns emptySet()
        
        val generalPapers = List(10) { i ->
            mockk<ArxivPaper> {
                coEvery { arxivId } returns "id_$i"
                coEvery { allCategories } returns listOf(mockk { coEvery { categoryId } returns "cs.IT" })
            }
        }
        
        
        coEvery { paperRepository.getCachedPapers(null, any()) } returnsMany listOf(emptyList(), generalPapers)

        
        val result = compileNewTriageUseCase()

        
        assertEquals(5, result.papers.size)
        coVerify { paperRepository.fetchAndCachePapers(null, 10) }
    }
}
