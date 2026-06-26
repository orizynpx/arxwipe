package io.github.orizynpx.arxwipe.data.repository

import io.github.orizynpx.arxwipe.data.local.dao.ArxwipeDao
import io.github.orizynpx.arxwipe.data.local.entity.SwipeEntity
import io.github.orizynpx.arxwipe.data.local.entity.TriageQueueEntity
import io.github.orizynpx.arxwipe.data.local.entity.toDomain
import io.github.orizynpx.arxwipe.domain.model.SwipeInteraction
import io.github.orizynpx.arxwipe.domain.model.SwipeType
import io.github.orizynpx.arxwipe.domain.model.Triage
import io.github.orizynpx.arxwipe.domain.repository.InteractionRepository
import javax.inject.Inject
import kotlin.time.Instant
import kotlin.uuid.Uuid

class InteractionRepositoryImpl @Inject constructor(
    private val dao: ArxwipeDao
) : InteractionRepository {

    override suspend fun recordSwipe(paperId: String, type: SwipeType) {
        val swipeId = Uuid.parse(java.util.UUID.randomUUID().toString())
        dao.insertSwipe(
            SwipeEntity(
                swipeId = swipeId.toString(),
                paperId = paperId,
                type = type.name,
                interactedAtMillis = System.currentTimeMillis()
            )
        )
    }

    override suspend fun getActiveTriage(): Triage? {
        val papers = dao.getTriagePapers().map { it.toDomain() }
        if (papers.isEmpty()) return null
        return Triage(
            triageId = Uuid.parse(java.util.UUID.randomUUID().toString()),
            papers = papers
        )
    }

    override suspend fun saveTriage(triage: Triage) {
        dao.clearTriageQueue()
        val queueEntities = triage.papers.mapIndexed { index, paper ->
            TriageQueueEntity(paper.arxivId, index)
        }
        dao.insertTriageQueue(queueEntities)
    }

    override suspend fun undoLastSwipe(): SwipeInteraction? {
        val lastSwipe = dao.getLastInteraction() ?: return null
        dao.deleteSwipe(lastSwipe.swipeId)
        return SwipeInteraction(
            swipeId = Uuid.parse(lastSwipe.swipeId),
            paperId = lastSwipe.paperId,
            type = SwipeType.valueOf(lastSwipe.type),
            interactedAt = Instant.fromEpochMilliseconds(lastSwipe.interactedAtMillis)
        )
    }
}