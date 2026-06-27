package io.github.orizynpx.arxwipe.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "paper_author_cross_ref",
    primaryKeys = ["arxivId", "authorId"],
    indices = [Index("authorId")]
)
data class PaperAuthorCrossRef(
    val arxivId: String,
    val authorId: String
)
