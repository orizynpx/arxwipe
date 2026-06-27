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
) {
    val hasPdf: Boolean get() = !pdfUrl.isNullOrBlank()

    val hasHtml: Boolean get() = !htmlUrl.isNullOrBlank()

    val formattedAuthors: String
        get() = authors.joinToString(", ") { it.name }

    fun matchesQuery(query: String): Boolean {
        if (query.isBlank()) return true
        val lowercaseQuery = query.lowercase()
        return title.lowercase().contains(lowercaseQuery) ||
                summary.lowercase().contains(lowercaseQuery) ||
                authors.any { it.name.lowercase().contains(lowercaseQuery) }
    }
}
