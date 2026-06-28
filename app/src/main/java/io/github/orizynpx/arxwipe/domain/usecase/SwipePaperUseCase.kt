package io.github.orizynpx.arxwipe.domain.usecase

import io.github.orizynpx.arxwipe.domain.model.SwipeType
import io.github.orizynpx.arxwipe.domain.repository.CollectionRepository
import io.github.orizynpx.arxwipe.domain.repository.InteractionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SwipePaperUseCase @Inject constructor(
    private val interactionRepository: InteractionRepository,
    private val collectionRepository: CollectionRepository,
) {
    suspend operator fun invoke(paperId: String, type: SwipeType) {
        interactionRepository.recordSwipe(paperId, type)

        if (type == SwipeType.SAVE) {
            val collections = collectionRepository.getUserCollections().first()
            val readLaterCollection = collections.find { it.name == "Read Later" }
            
            val collectionId = readLaterCollection?.collectionId 
                ?: collectionRepository.createCollection("Read Later").collectionId
            
            collectionRepository.addPaperToCollection(paperId, collectionId)
        }
    }
}
