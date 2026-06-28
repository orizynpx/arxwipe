package io.github.orizynpx.arxwipe.domain.repository

import io.github.orizynpx.arxwipe.domain.model.PaperCollection
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface CollectionRepository {
    fun getUserCollections(): Flow<List<PaperCollection>>
    suspend fun createCollection(name: String): PaperCollection
    suspend fun updateCollection(collectionId: Uuid, name: String)
    suspend fun deleteCollection(collectionId: Uuid)
    suspend fun addPaperToCollection(paperId: String, collectionId: Uuid)
    suspend fun removePaperFromCollection(paperId: String, collectionId: Uuid)
}
