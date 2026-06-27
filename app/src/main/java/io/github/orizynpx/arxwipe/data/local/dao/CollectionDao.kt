package io.github.orizynpx.arxwipe.data.local.dao

import androidx.room.*
import io.github.orizynpx.arxwipe.data.local.entity.CollectionEntity
import io.github.orizynpx.arxwipe.data.local.entity.CollectionPaperCrossRef
import io.github.orizynpx.arxwipe.data.local.entity.CollectionWithPapers
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections")
    fun getUserCollections(): Flow<List<CollectionEntity>>

    @Transaction
    @Query("SELECT * FROM collections")
    fun getUserCollectionsWithPapers(): Flow<List<CollectionWithPapers>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollectionPaperCrossRef(crossRef: CollectionPaperCrossRef)

    @Delete
    suspend fun deleteCollectionPaperCrossRef(crossRef: CollectionPaperCrossRef)

    @Query("DELETE FROM collections WHERE collectionId = :collectionId")
    suspend fun deleteCollectionById(collectionId: String)
}
