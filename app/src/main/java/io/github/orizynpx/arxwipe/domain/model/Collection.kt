package io.github.orizynpx.arxwipe.domain.model

import kotlin.uuid.Uuid

data class Collection(
    val collectionId: Uuid,
    val name: String,
    val papers: List<ArxivPaper>
)
