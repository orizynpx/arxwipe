package io.github.orizynpx.arxwipe.data.repository

import io.github.orizynpx.arxwipe.data.local.dao.ArxwipeDao
import io.github.orizynpx.arxwipe.data.local.entity.CollectionEntity
import io.github.orizynpx.arxwipe.data.local.entity.toDomain
import io.github.orizynpx.arxwipe.domain.model.PaperCollection
import io.github.orizynpx.arxwipe.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.uuid.Uuid

class CollectionRepositoryImpl @Inject constructor(
    private val dao: ArxwipeDao
) : CollectionRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override fun getUserCollections(): Flow<List<PaperCollection>> {
        return dao.observeCollections().map { entities ->
            entities.map { entity ->
                val paperIds: List<String> = json.decodeFromString(entity.paperIdsJson)
                val papers = paperIds.mapNotNull { id -> dao.getPaperById(id)?.toDomain() }
                PaperCollection(
                    collectionId = Uuid.parse(entity.collectionId),
                    name = entity.name,
                    papers = papers
                )
            }
        }
    }

    override suspend fun createCollection(name: String): PaperCollection {
        val id = Uuid.parse(java.util.UUID.randomUUID().toString())
        val entity = CollectionEntity(id.toString(), name, "[]")
        dao.insertCollection(entity)
        return PaperCollection(id, name, emptyList())
    }

    override suspend fun updateCollection(collectionId: Uuid, name: String) {
        val existing = dao.getCollections().find { it.collectionId == collectionId.toString() }
        val paperIdsJson = existing?.paperIdsJson ?: "[]"
        dao.insertCollection(CollectionEntity(collectionId.toString(), name, paperIdsJson))
    }

    override suspend fun deleteCollection(collectionId: Uuid) {
        dao.deleteCollection(collectionId.toString())
    }

    override suspend fun addPaperToCollection(paperId: String, collectionId: Uuid) {
        val existing = dao.getCollections().find { it.collectionId == collectionId.toString() } ?: return
        val paperIds: List<String> = json.decodeFromString(existing.paperIdsJson)
        if (paperId !in paperIds) {
            val updated = paperIds + paperId
            dao.insertCollection(existing.copy(paperIdsJson = json.encodeToString(updated)))
        }
    }

    override suspend fun removePaperFromCollection(paperId: String, collectionId: Uuid) {
        val existing = dao.getCollections().find { it.collectionId == collectionId.toString() } ?: return
        val paperIds: List<String> = json.decodeFromString(existing.paperIdsJson)
        if (paperId in paperIds) {
            val updated = paperIds - paperId
            dao.insertCollection(existing.copy(paperIdsJson = json.encodeToString(updated)))
        }
    }
}