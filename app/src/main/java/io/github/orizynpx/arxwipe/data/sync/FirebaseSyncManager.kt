package io.github.orizynpx.arxwipe.data.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import io.github.orizynpx.arxwipe.data.local.dao.CollectionDao
import io.github.orizynpx.arxwipe.data.local.dao.InteractionDao
import io.github.orizynpx.arxwipe.data.local.entity.CollectionEntity
import io.github.orizynpx.arxwipe.data.local.entity.SwipeInteractionEntity
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
    private val interactionDao: InteractionDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var collectionsListener: ListenerRegistration? = null
    private var interactionsListener: ListenerRegistration? = null

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

                snapshots?.forEach { doc ->
                    val name = doc.getString("name") ?: ""
                    val id = doc.id
                    
                    scope.launch {
                        
                        
                        collectionDao.insertCollection(CollectionEntity(id, name))
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

                snapshots?.forEach { doc ->
                    val id = doc.id
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
                            val local = interactionDao.getLastInteraction() 
                            
                            
                            interactionDao.insertSwipe(
                                SwipeInteractionEntity(id, paperId, type, interactedAt)
                            )
                        }
                    }
                }
            }
    }

    fun stopSync() {
        collectionsListener?.remove()
        interactionsListener?.remove()
        collectionsListener = null
        interactionsListener = null
    }
}
