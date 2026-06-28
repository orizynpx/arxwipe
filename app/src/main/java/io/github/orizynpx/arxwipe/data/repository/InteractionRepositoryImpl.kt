package io.github.orizynpx.arxwipe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.github.orizynpx.arxwipe.data.local.dao.InteractionDao
import io.github.orizynpx.arxwipe.data.local.dao.PaperDao
import io.github.orizynpx.arxwipe.data.local.entity.SwipeInteractionEntity
import io.github.orizynpx.arxwipe.data.local.entity.TriagePaperCrossRef
import io.github.orizynpx.arxwipe.data.local.entity.toDomain
import io.github.orizynpx.arxwipe.domain.model.SwipeInteraction
import io.github.orizynpx.arxwipe.domain.model.SwipeType
import io.github.orizynpx.arxwipe.domain.model.Triage
import io.github.orizynpx.arxwipe.domain.repository.InteractionRepository
import io.github.orizynpx.arxwipe.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class InteractionRepositoryImpl @Inject constructor(
    private val interactionDao: InteractionDao,
    private val paperDao: PaperDao,
    private val preferencesRepository: PreferencesRepository,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : InteractionRepository {

    override suspend fun recordSwipe(paperId: String, type: SwipeType) {
        val swipe = SwipeInteractionEntity(
            swipeId = Uuid.random().toString(),
            paperId = paperId,
            type = type.name,
            interactedAt = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        )
        interactionDao.insertSwipe(swipe)

        firebaseAuth.currentUser?.uid?.let { userId ->
            val data = mapOf(
                "paperId" to swipe.paperId,
                "type" to swipe.type,
                "interactedAt" to swipe.interactedAt.toString()
            )
            firestore.collection("users").document(userId)
                .collection("interactions").document(swipe.swipeId)
                .set(data)
        }
        
        val currentIndex = preferencesRepository.getCurrentTriageIndex().first()
        preferencesRepository.saveTriageIndex(currentIndex + 1)
    }

    override suspend fun getActiveTriage(): Triage? {
        val activePapers = paperDao.getActivePapersList()
        if (activePapers.isEmpty()) return null
        
        return Triage(
            triageId = Uuid.random(),
            papers = activePapers.map { it.toDomain() }
        )
    }

    override suspend fun saveTriage(triage: Triage) {
        val crossRefs = triage.papers.map { TriagePaperCrossRef(it.arxivId) }
        interactionDao.insertTriagePapers(crossRefs)
    }

    override suspend fun undoLastSwipe(): SwipeInteraction? {
        val lastSwipeEntity = interactionDao.getLastInteraction() ?: return null
        interactionDao.undoLastSwipe()
        
        firebaseAuth.currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId)
                .collection("interactions").document(lastSwipeEntity.swipeId)
                .delete()
        }

        val currentIndex = preferencesRepository.getCurrentTriageIndex().first()
        if (currentIndex > 0) {
            preferencesRepository.saveTriageIndex(currentIndex - 1)
        }
        
        return SwipeInteraction(
            swipeId = Uuid.parse(lastSwipeEntity.swipeId),
            paperId = lastSwipeEntity.paperId,
            type = SwipeType.valueOf(lastSwipeEntity.type),
            interactedAt = lastSwipeEntity.interactedAt
        )
    }

    override suspend fun clearTriage() {
        interactionDao.clearTriage()
    }

    override suspend fun getSwipedPaperIds(): Set<String> {
        return interactionDao.getAllSwipedPaperIds().toSet()
    }

    override fun getSwipedPaperIdsFlow(): Flow<Set<String>> {
        return interactionDao.getAllSwipedPaperIdsFlow().map { it.toSet() }
    }

    override suspend fun replaceTriage(triage: Triage) {
        val crossRefs = triage.papers.map { TriagePaperCrossRef(it.arxivId) }
        interactionDao.replaceTriage(crossRefs)
    }
}
