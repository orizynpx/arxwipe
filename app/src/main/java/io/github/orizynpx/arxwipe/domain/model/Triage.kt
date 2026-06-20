package io.github.orizynpx.arxwipe.domain.model

import kotlin.uuid.Uuid

data class Triage(
    val triageId: Uuid,
    val papers: List<ArxivPaper>,
)
