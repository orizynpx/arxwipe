package io.github.orizynpx.arxwipe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.orizynpx.arxwipe.domain.model.*
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class AuthorDb(val id: String, val name: String)

@Serializable
data class CategoryDb(
    val id: String,
    val displayName: String,
    val group: String,
    val desc: String
)

@Entity(tableName = "cached_papers")
data class PaperEntity(
    @PrimaryKey val arxivId: String,
    val title: String,
    val summary: String,
    val journalReference: String?,
    val abstractUrl: String?,
    val pdfUrl: String?,
    val htmlUrl: String?,
    val publishedAtMillis: Long,
    val updatedAtMillis: Long?,
    val comment: String?,
    val authorsJson: String,      // List<AuthorDb> serialized
    val categoriesJson: String,   // List<CategoryDb> serialized
    val primaryCategoryJson: String // CategoryDb serialized
)

@Entity(tableName = "user_collections")
data class CollectionEntity(
    @PrimaryKey val collectionId: String,
    val name: String,
    val paperIdsJson: String = "[]" // List<String> of referenced arxivIds
)

@Entity(tableName = "swipe_interactions")
data class SwipeEntity(
    @PrimaryKey val swipeId: String,
    val paperId: String,
    val type: String, // SwipeType (SAVE / DISMISS)
    val interactedAtMillis: Long
)

@Entity(tableName = "triage_queue")
data class TriageQueueEntity(
    @PrimaryKey val arxivId: String,
    val orderIndex: Int
)