package io.github.orizynpx.arxwipe.domain.usecase

import io.github.orizynpx.arxwipe.domain.model.SwipeType
import io.github.orizynpx.arxwipe.domain.repository.CollectionRepository
import io.github.orizynpx.arxwipe.domain.repository.InteractionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UndoSwipeUseCase @Inject constructor(
    private val interactionRepository: InteractionRepository,
    private val collectionRepository: CollectionRepository,
) {
    suspend operator fun invoke() {
        val lastSwipe = interactionRepository.undoLastSwipe() ?: return

        if (lastSwipe.type == SwipeType.SAVE) {
            val collections = collectionRepository.getUserCollections().first()
            val readLaterCollection = collections.find { it.name == "Read Later" }
            
            readLaterCollection?.let {
                collectionRepository.removePaperFromCollection(lastSwipe.paperId, it.collectionId)
            }
        }
    }
}
