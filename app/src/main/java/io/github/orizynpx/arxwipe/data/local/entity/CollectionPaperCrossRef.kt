package io.github.orizynpx.arxwipe.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "collection_paper_cross_ref",
    primaryKeys = ["collectionId", "arxivId"],
    indices = [Index("arxivId")]
)
data class CollectionPaperCrossRef(
    val collectionId: String,
    val arxivId: String
)
