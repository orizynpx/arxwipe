package io.github.orizynpx.arxwipe.domain.model

import kotlin.uuid.Uuid

data class PaperAuthor(
    val authorId: Uuid,
    val name: String
)
