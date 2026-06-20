package io.github.orizynpx.arxwipe.domain.model

import kotlin.time.Instant

data class ArxivPaper(
    val arxivId: String,

    val title: String,
    val authors: List<PaperAuthor>,
    val summary: String,
    val journalReference: String?,

    val primaryCategory: PaperCategory,
    val allCategories: List<PaperCategory>,

    val abstractUrl: String?,
    val pdfUrl: String?,
    val htmlUrl: String?,

    val publishedAt: Instant,
    val updatedAt: Instant?,

    val comment: String?,
)
