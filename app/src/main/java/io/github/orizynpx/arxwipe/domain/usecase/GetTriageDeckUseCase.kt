package io.github.orizynpx.arxwipe.domain.usecase

import io.github.orizynpx.arxwipe.domain.model.Triage
import io.github.orizynpx.arxwipe.domain.repository.InteractionRepository
import io.github.orizynpx.arxwipe.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetTriageDeckUseCase @Inject constructor(
    private val interactionRepository: InteractionRepository,
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(): Triage? {
        val activeTriage = interactionRepository.getActiveTriage() ?: return null
        val currentSwipedIndex = preferencesRepository.getCurrentTriageIndex().first()

        return if (currentSwipedIndex < activeTriage.papers.size) {
            activeTriage
        } else {
            null
        }
    }
}