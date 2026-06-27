package io.github.orizynpx.arxwipe.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PaperWithAuthors(
    @Embedded val paper: PaperEntity,
    @Relation(
        parentColumn = "arxivId",
        entityColumn = "authorId",
        associateBy = Junction(PaperAuthorCrossRef::class)
    )
    val authors: List<AuthorEntity>
)
