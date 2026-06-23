package io.github.orizynpx.arxwipe.domain.usecase

import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.Triage
import io.github.orizynpx.arxwipe.domain.repository.InteractionRepository
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.uuid.Uuid

class GetTriageDeckUseCase @Inject constructor(
    private val interactionRepository: InteractionRepository,
    private val paperRepository: PaperRepository,
) {
    suspend operator fun invoke(): Triage {
        val activeTriage = interactionRepository.getActiveTriage()
        if (activeTriage != null) return activeTriage

        val prefs = paperRepository.getOnboardingPreferences().first()
        val allPapers = mutableListOf<ArxivPaper>()

        for (categoryId in prefs.selectedCategoryIds) {
            val papers = paperRepository.getDiscoveryFeed(
                categoryId = categoryId,
                limit = prefs.batchSize
            )
            allPapers.addAll(papers)
        }

        // If no categories selected or no papers found, try getting general feed
        if (allPapers.isEmpty()) {
            val papers = paperRepository.getDiscoveryFeed(
                categoryId = null,
                limit = prefs.batchSize
            )
            allPapers.addAll(papers)
        }

        val newTriage = Triage(
            triageId = Uuid.random(),
            papers = allPapers.distinctBy { it.arxivId }
        )

        interactionRepository.saveTriage(newTriage)
        return newTriage
    }
}
