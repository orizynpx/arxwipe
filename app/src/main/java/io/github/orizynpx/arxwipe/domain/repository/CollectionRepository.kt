package io.github.orizynpx.arxwipe.domain.repository

import io.github.orizynpx.arxwipe.domain.model.PaperCollection
import kotlin.uuid.Uuid

interface CollectionRepository {
    suspend fun getUserCollections(): List<PaperCollection>
    suspend fun createCollection(name: String): PaperCollection
    suspend fun addPaperToCollection(paperId: String, collectionId: Uuid)
}
