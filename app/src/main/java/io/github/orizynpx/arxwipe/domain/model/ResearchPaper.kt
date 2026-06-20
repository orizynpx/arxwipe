package io.github.orizynpx.arxwipe.domain.model

import kotlin.time.Instant

data class ResearchPaper(
    val arxivId: String,
    val title: String,
    val authors: List<String>,
    val summary: String,
    val primaryCategory: String,
    val allCategories: List<String>,
    val pdfUrl: String?,
    val htmlUrl: String?,
    val publishedAt: Instant,
)
