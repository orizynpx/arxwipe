package io.github.orizynpx.arxwipe.domain.usecase

import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.Triage
import io.github.orizynpx.arxwipe.domain.repository.InteractionRepository
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import io.github.orizynpx.arxwipe.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CompileNewTriageUseCase @Inject constructor(
    private val interactionRepository: InteractionRepository,
    private val paperRepository: PaperRepository,
    private val preferencesRepository: PreferencesRepository
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(force: Boolean = false): Triage {
        val prefs = preferencesRepository.getOnboardingPreferences().first()
        val batchSize = prefs.batchSize.coerceAtLeast(1)
        val requiredThreshold = batchSize * 2
        val categories = prefs.selectedCategoryIds

        
        val lastConfigCategories = preferencesRepository.getLastTriageConfigCategories().first()
        val lastConfigBatchSize = preferencesRepository.getLastTriageConfigBatchSize().first()
        val isMismatch = lastConfigCategories != categories.toSet() || lastConfigBatchSize != batchSize
        
        if (isMismatch) {
            Timber.d("Preference mismatch detected (Categories: $lastConfigCategories -> ${categories.toSet()}, Batch: $lastConfigBatchSize -> $batchSize). Forcing re-compile.")
            interactionRepository.clearTriage()
        }

        
        val activeTriage = interactionRepository.getActiveTriage()
        val currentIndex = preferencesRepository.getCurrentTriageIndex().first()
        
        if (!force && !isMismatch && activeTriage != null && currentIndex < activeTriage.papers.size) {
            Timber.d("Active triage still has ${activeTriage.papers.size - currentIndex} papers. Reusing.")
            return activeTriage
        }

        
        val lastTriageDate = preferencesRepository.getLastTriageDate().first()
        val today = LocalDate.now().toString()
        if (!force && !isMismatch && lastTriageDate == today) {
            Timber.d("Already compiled a triage today ($today). Skipping auto-compile.")
            
            return activeTriage ?: Triage(Uuid.random(), emptyList())
        }

        
        var candidates = collectAndValidateCandidates(categories, categories)

        
        if (candidates.size < requiredThreshold) {
            Timber.d("Only ${candidates.size} unswiped local candidates (<$requiredThreshold); fetching from remote.")
            fetchForCategories(categories, requiredThreshold)
            candidates = collectAndValidateCandidates(categories, categories)
        }

        
        if (candidates.isEmpty()) {
            Timber.w("Preferred categories $categories returned no papers locally or remotely. Falling back to general.")
            paperRepository.fetchAndCachePapers(null, requiredThreshold)
            candidates = collectAndValidateCandidates(emptyList(), categories)
            
            if (candidates.isEmpty()) {
                candidates = collectAndValidateCandidates(emptyList(), emptyList())
            }
        }

        
        preferencesRepository.saveTriageIndex(0)

        val batch = candidates.take(batchSize)
        val newTriage = Triage(
            triageId = Uuid.random(),
            papers = batch
        )
        interactionRepository.replaceTriage(newTriage)
        
        preferencesRepository.saveLastTriageDate(today)
        preferencesRepository.saveLastTriageConfigCategories(categories.toSet())
        preferencesRepository.saveLastTriageConfigBatchSize(batchSize)
        Timber.d("Compiled new triage with ${batch.size} papers.")
        return newTriage
    }

    private suspend fun fetchForCategories(categories: List<String>, targetCount: Int) {
        if (categories.isEmpty()) {
            paperRepository.fetchAndCachePapers(null, targetCount)
        } else {
            for (categoryId in categories) {
                
                paperRepository.fetchAndCachePapers(categoryId, 100)
            }
        }
    }

    private suspend fun collectAndValidateCandidates(
        fetchCategories: List<String>, 
        filterPrefs: List<String>
    ): List<ArxivPaper> {
        val swipedIds = interactionRepository.getSwipedPaperIds()
        val pool = mutableListOf<ArxivPaper>()
        
        if (fetchCategories.isEmpty()) {
            pool.addAll(paperRepository.getCachedPapers(null, CANDIDATE_POOL_LIMIT))
        } else {
            for (categoryId in fetchCategories) {
                pool.addAll(paperRepository.getCachedPapers(categoryId, CANDIDATE_POOL_LIMIT))
            }
        }
        
        val filtered = pool.distinctBy { it.arxivId }
            .filter { paper -> 
                if (filterPrefs.isEmpty()) true else filterPrefs.any { selectedId ->
                    val prefix = selectedId.removeSuffix(".*")
                    paper.allCategories.any { it.categoryId.startsWith(prefix) }
                }
            }
            .filter { it.arxivId !in swipedIds }

        Timber.d("Candidates collected: fetchCategories=$fetchCategories, filterPrefs=$filterPrefs, poolSize=${pool.size}, resultSize=${filtered.size}")
        return filtered
    }

    private companion object {
        const val CANDIDATE_POOL_LIMIT = 500
    }
}