package io.github.orizynpx.arxwipe.domain.usecase

import io.github.orizynpx.arxwipe.domain.model.PaperCollection
import io.github.orizynpx.arxwipe.domain.model.SwipeInteraction
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

class UndoSwipeUseCaseTest {

    private lateinit var interactionRepository: InteractionRepository
    private lateinit var collectionRepository: CollectionRepository
    private lateinit var undoSwipeUseCase: UndoSwipeUseCase

    @Before
    fun setUp() {
        interactionRepository = mockk(relaxed = true)
        collectionRepository = mockk(relaxed = true)
        undoSwipeUseCase = UndoSwipeUseCase(interactionRepository, collectionRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `when last swipe was SAVE, undo swipe and remove from Read Later collection`() = runTest {
        
        val paperId = "paper-123"
        val collectionId = Uuid.random()
        val lastSwipe = SwipeInteraction(
            swipeId = Uuid.random(),
            paperId = paperId,
            type = SwipeType.SAVE,
            interactedAt = mockk()
        )
        val readLaterCollection = PaperCollection(collectionId, "Read Later", emptyList())

        coEvery { interactionRepository.undoLastSwipe() } returns lastSwipe
        coEvery { collectionRepository.getUserCollections() } returns flowOf(listOf(readLaterCollection))

        
        undoSwipeUseCase()

        
        coVerify(exactly = 1) { interactionRepository.undoLastSwipe() }
        coVerify(exactly = 1) { collectionRepository.removePaperFromCollection(paperId, collectionId) }
        confirmVerified(interactionRepository, collectionRepository)
    }

    @Test
    fun `when last swipe was DISMISS, undo swipe and do not touch collection repository`() = runTest {
        
        val lastSwipe = SwipeInteraction(
            swipeId = Uuid.random(),
            paperId = "paper-456",
            type = SwipeType.DISMISS,
            interactedAt = mockk()
        )

        coEvery { interactionRepository.undoLastSwipe() } returns lastSwipe

        
        undoSwipeUseCase()

        
        coVerify(exactly = 1) { interactionRepository.undoLastSwipe() }
        coVerify(exactly = 0) { collectionRepository.getUserCollections() }
        coVerify(exactly = 0) { collectionRepository.removePaperFromCollection(any(), any()) }
        confirmVerified(interactionRepository, collectionRepository)
    }
}
