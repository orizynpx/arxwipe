package io.github.orizynpx.arxwipe.data.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import io.github.orizynpx.arxwipe.data.local.dao.CollectionDao
import io.github.orizynpx.arxwipe.data.local.dao.InteractionDao
import io.github.orizynpx.arxwipe.data.local.dao.PaperDao
import io.github.orizynpx.arxwipe.data.local.entity.CollectionEntity
import io.github.orizynpx.arxwipe.data.local.entity.CollectionPaperCrossRef
import io.github.orizynpx.arxwipe.data.local.entity.SwipeInteractionEntity
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant

@Singleton
class FirebaseSyncManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val collectionDao: CollectionDao,
    private val interactionDao: InteractionDao,
    private val paperDao: PaperDao,
    private val paperRepository: PaperRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var collectionsListener: ListenerRegistration? = null
    private var interactionsListener: ListenerRegistration? = null
    private val paperListeners = mutableMapOf<String, ListenerRegistration>()

    fun startRealTimeSync() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        stopSync()

        Timber.d("Starting real-time sync for user: $userId")

        
        collectionsListener = firestore.collection("users").document(userId)
            .collection("collections")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Timber.w(e, "Listen failed for collections")
                    return@addSnapshotListener
                }

                val remoteCollectionIds = snapshots?.map { it.id } ?: emptyList()
                
                
                val currentTrackedIds = paperListeners.keys.toList()
                currentTrackedIds.forEach { id ->
                    if (id !in remoteCollectionIds) {
                        paperListeners[id]?.remove()
                        paperListeners.remove(id)
                    }
                }

                snapshots?.documentChanges?.forEach { change ->
                    val doc = change.document
                    val id = doc.id
                    val name = doc.getString("name") ?: ""

                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            scope.launch {
                                collectionDao.insertCollection(CollectionEntity(id, name))
                            }
                            
                            if (!paperListeners.containsKey(id)) {
                                startPaperSync(userId, id)
                            }
                        }
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            scope.launch {
                                collectionDao.deleteCollectionById(id)
                            }
                            paperListeners[id]?.remove()
                            paperListeners.remove(id)
                        }
                    }
                }
            }

        
        interactionsListener = firestore.collection("users").document(userId)
            .collection("interactions")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Timber.w(e, "Listen failed for interactions")
                    return@addSnapshotListener
                }

                snapshots?.documentChanges?.forEach { change ->
                    val doc = change.document
                    val id = doc.id

                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            val paperId = doc.getString("paperId") ?: ""
                            val type = doc.getString("type") ?: ""
                            val interactedAtStr = doc.getString("interactedAt")
                            val interactedAt = try {
                                interactedAtStr?.let { Instant.parse(it) }
                            } catch (ex: Exception) {
                                null
                            }

                            if (interactedAt != null) {
                                scope.launch {
                                    interactionDao.insertSwipe(
                                        SwipeInteractionEntity(id, paperId, type, interactedAt)
                                    )
                                }
                            }
                        }
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            scope.launch {
                                interactionDao.deleteSwipe(id)
                            }
                        }
                    }
                }
            }
    }

    private fun startPaperSync(userId: String, collectionId: String) {
        val listener = firestore.collection("users").document(userId)
            .collection("collections").document(collectionId)
            .collection("papers")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Timber.w(e, "Listen failed for papers in collection: $collectionId")
                    return@addSnapshotListener
                }

                snapshots?.documentChanges?.forEach { change ->
                    val doc = change.document
                    val paperId = doc.id

                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                            scope.launch {
                                
                                val localPaper = paperDao.getPaperById(paperId)
                                if (localPaper == null) {
                                    paperRepository.fetchAndSavePaper(paperId)
                                }
                                collectionDao.insertCollectionPaperCrossRef(
                                    CollectionPaperCrossRef(collectionId, paperId)
                                )
                            }
                        }
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            scope.launch {
                                collectionDao.deleteCollectionPaperCrossRef(
                                    CollectionPaperCrossRef(collectionId, paperId)
                                )
                            }
                        }
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            
                        }
                    }
                }
            }
        paperListeners[collectionId] = listener
    }

    fun stopSync() {
        collectionsListener?.remove()
        interactionsListener?.remove()
        paperListeners.values.forEach { it.remove() }
        paperListeners.clear()
        collectionsListener = null
        interactionsListener = null
    }
}
