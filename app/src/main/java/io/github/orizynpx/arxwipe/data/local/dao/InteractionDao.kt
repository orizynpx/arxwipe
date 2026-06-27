package io.github.orizynpx.arxwipe.data.local.dao

import androidx.room.*
import io.github.orizynpx.arxwipe.data.local.entity.SwipeInteractionEntity
import io.github.orizynpx.arxwipe.data.local.entity.TriagePaperCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
abstract class InteractionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSwipe(swipe: SwipeInteractionEntity)

    @Query("DELETE FROM swipe_interactions WHERE swipeId = :swipeId")
    abstract suspend fun deleteSwipe(swipeId: String)

    @Query("SELECT * FROM swipe_interactions ORDER BY interactedAt DESC LIMIT 1")
    abstract suspend fun getLastInteraction(): SwipeInteractionEntity?

    @Transaction
    open suspend fun undoLastSwipe() {
        getLastInteraction()?.let {
            deleteSwipe(it.swipeId)
        }
    }

    @Query("DELETE FROM triage_paper_cross_ref")
    abstract suspend fun clearTriage()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertTriagePaper(triagePaper: TriagePaperCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertTriagePapers(triagePapers: List<TriagePaperCrossRef>)

    @Query("DELETE FROM triage_paper_cross_ref WHERE arxivId = :arxivId")
    abstract suspend fun removeFromTriage(arxivId: String)

    @Transaction
    open suspend fun replaceTriage(triagePapers: List<TriagePaperCrossRef>) {
        clearTriage()
        insertTriagePapers(triagePapers)
    }

    @Query("SELECT * FROM triage_paper_cross_ref")
    abstract fun getTriageQueue(): Flow<List<TriagePaperCrossRef>>

    @Query("SELECT paperId FROM swipe_interactions")
    abstract suspend fun getAllSwipedPaperIds(): List<String>

    @Query("SELECT paperId FROM swipe_interactions")
    abstract fun getAllSwipedPaperIdsFlow(): Flow<List<String>>
}
