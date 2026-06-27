package io.github.orizynpx.arxwipe.domain.usecase

import io.github.orizynpx.arxwipe.domain.model.PaperCollection
import io.github.orizynpx.arxwipe.domain.model.SwipeType
import io.github.orizynpx.arxwipe.domain.repository.CollectionRepository
import io.github.orizynpx.arxwipe.domain.repository.InteractionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.uuid.Uuid

class SwipePaperUseCaseTest {

    private lateinit var interactionRepository: InteractionRepository
    private lateinit var collectionRepository: CollectionRepository
    private lateinit var swipePaperUseCase: SwipePaperUseCase

    @Before
    fun setUp() {
        interactionRepository = mockk(relaxed = true)
        collectionRepository = mockk(relaxed = true)
        swipePaperUseCase = SwipePaperUseCase(interactionRepository, collectionRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `when swiping SAVE, record swipe and add to Read Later collection`() = runTest {
        
        val paperId = "paper-123"
        val collectionId = Uuid.random()
        val readLaterCollection = PaperCollection(collectionId, "Read Later", emptyList())
        
        coEvery { collectionRepository.getUserCollections() } returns flowOf(listOf(readLaterCollection))

        
        swipePaperUseCase(paperId, SwipeType.SAVE)

        
        coVerify(exactly = 1) { interactionRepository.recordSwipe(paperId, SwipeType.SAVE) }
        coVerify(exactly = 1) { collectionRepository.addPaperToCollection(paperId, collectionId) }
        confirmVerified(interactionRepository, collectionRepository)
    }

    @Test
    fun `when swiping DISMISS, record swipe and do not touch collection repository`() = runTest {
        
        val paperId = "paper-456"

        
        swipePaperUseCase(paperId, SwipeType.DISMISS)

        
        coVerify(exactly = 1) { interactionRepository.recordSwipe(paperId, SwipeType.DISMISS) }
        coVerify(exactly = 0) { collectionRepository.getUserCollections() }
        coVerify(exactly = 0) { collectionRepository.addPaperToCollection(any(), any()) }
        confirmVerified(interactionRepository, collectionRepository)
    }
}
