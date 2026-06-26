package io.github.orizynpx.arxwipe.data.local.dao

import androidx.room.*
import io.github.orizynpx.arxwipe.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArxwipeDao {
    // --- Papers Cache ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPapers(papers: List<PaperEntity>)

    @Query("SELECT * FROM cached_papers WHERE arxivId = :arxivId")
    suspend fun getPaperById(arxivId: String): PaperEntity?

    @Query("SELECT * FROM cached_papers LIMIT :limit")
    suspend fun getAllCachedPapers(limit: Int): List<PaperEntity>

    // --- Collections ---
    @Query("SELECT * FROM user_collections")
    fun observeCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM user_collections")
    suspend fun getCollections(): List<CollectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity)

    @Query("DELETE FROM user_collections WHERE collectionId = :collectionId")
    suspend fun deleteCollection(collectionId: String)

    // --- Triage Stack Queue ---
    @Query("DELETE FROM triage_queue")
    suspend fun clearTriageQueue()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTriageQueue(entries: List<TriageQueueEntity>)

    @Query("SELECT * FROM cached_papers WHERE arxivId IN (SELECT arxivId FROM triage_queue ORDER BY orderIndex ASC)")
    fun observeTriagePapers(): Flow<List<PaperEntity>>

    @Query("SELECT * FROM cached_papers WHERE arxivId IN (SELECT arxivId FROM triage_queue ORDER BY orderIndex ASC)")
    suspend fun getTriagePapers(): List<PaperEntity>

    // --- Interactions ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwipe(swipe: SwipeEntity)

    @Query("SELECT * FROM swipe_interactions ORDER BY interactedAtMillis DESC LIMIT 1")
    suspend fun getLastInteraction(): SwipeEntity?

    @Query("DELETE FROM swipe_interactions WHERE swipeId = :swipeId")
    suspend fun deleteSwipe(swipeId: String)

    @Query("SELECT paperId FROM swipe_interactions")
    suspend fun getSwipedPaperIds(): List<String>
}