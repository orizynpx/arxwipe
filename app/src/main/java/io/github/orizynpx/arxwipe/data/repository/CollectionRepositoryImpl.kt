package io.github.orizynpx.arxwipe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.github.orizynpx.arxwipe.data.local.dao.CollectionDao
import io.github.orizynpx.arxwipe.data.local.entity.CollectionEntity
import io.github.orizynpx.arxwipe.data.local.entity.CollectionPaperCrossRef
import io.github.orizynpx.arxwipe.data.local.entity.toDomain
import io.github.orizynpx.arxwipe.domain.model.PaperCollection
import io.github.orizynpx.arxwipe.domain.repository.CollectionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CollectionRepositoryImpl @Inject constructor(
    private val collectionDao: CollectionDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : CollectionRepository {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val collections = collectionDao.getUserCollections().first()
            val readLaterId = "00000000-0000-0000-0000-000000000000"
            if (collections.none { it.name == "Read Later" }) {
                collectionDao.insertCollection(
                    CollectionEntity(
                        collectionId = readLaterId, 
                        name = "Read Later"
                    )
                )
            }
            
            
            firebaseAuth.currentUser?.uid?.let { userId ->
                firestore.collection("users").document(userId)
                    .collection("collections").document(readLaterId)
                    .set(mapOf("name" to "Read Later"))
            }
        }
    }

    override fun getUserCollections(): Flow<List<PaperCollection>> {
        return collectionDao.getUserCollectionsWithPapers().map { collectionsWithPapers ->
            collectionsWithPapers.map { item ->
                PaperCollection(
                    collectionId = Uuid.parse(item.collection.collectionId),
                    name = item.collection.name,
                    papers = item.papers.map { it.toDomain() }
                )
            }
        }
    }

    override suspend fun createCollection(name: String): PaperCollection {
        val collectionId = Uuid.random()
        val entity = CollectionEntity(
            collectionId = collectionId.toString(),
            name = name
        )
        collectionDao.insertCollection(entity)
        
        firebaseAuth.currentUser?.uid?.let { userId ->
            val data = mapOf(
                "name" to name
            )
            firestore.collection("users").document(userId)
                .collection("collections").document(entity.collectionId)
                .set(data)
        }

        return PaperCollection(
            collectionId = collectionId,
            name = name,
            papers = emptyList()
        )
    }

    override suspend fun updateCollection(collectionId: Uuid, name: String) {
        collectionDao.insertCollection(
            CollectionEntity(
                collectionId = collectionId.toString(),
                name = name
            )
        )
        
        firebaseAuth.currentUser?.uid?.let { userId ->
            val data = mapOf(
                "name" to name
            )
            firestore.collection("users").document(userId)
                .collection("collections").document(collectionId.toString())
                .update(data)
        }
    }

    override suspend fun deleteCollection(collectionId: Uuid) {
        collectionDao.deleteCollectionById(collectionId.toString())

        firebaseAuth.currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId)
                .collection("collections").document(collectionId.toString())
                .delete()
        }
    }

    override suspend fun addPaperToCollection(paperId: String, collectionId: Uuid) {
        collectionDao.insertCollectionPaperCrossRef(
            CollectionPaperCrossRef(
                collectionId = collectionId.toString(),
                arxivId = paperId
            )
        )

        firebaseAuth.currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId)
                .collection("collections").document(collectionId.toString())
                .collection("papers").document(paperId)
                .set(mapOf("arxivId" to paperId))
        }
    }

    override suspend fun removePaperFromCollection(paperId: String, collectionId: Uuid) {
        collectionDao.deleteCollectionPaperCrossRef(
            CollectionPaperCrossRef(
                collectionId = collectionId.toString(),
                arxivId = paperId
            )
        )

        firebaseAuth.currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId)
                .collection("collections").document(collectionId.toString())
                .collection("papers").document(paperId)
                .delete()
        }
    }
}
