package io.github.orizynpx.arxwipe.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CollectionWithPapers(
    @Embedded val collection: CollectionEntity,
    @Relation(
        entity = PaperEntity::class,
        parentColumn = "collectionId",
        entityColumn = "arxivId",
        associateBy = Junction(CollectionPaperCrossRef::class)
    )
    val papers: List<PaperWithAuthors>
)
