package io.github.orizynpx.arxwipe.data.remote.dto

import io.github.orizynpx.arxwipe.domain.model.*
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class ArxivEntryDto(
    val id: String,
    val title: String,
    val summary: String,
    val published: String,
    val updated: String?,
    val authors: List<String>,
    val categories: List<String>,
    val pdfUrl: String?,
    val htmlUrl: String?
)

fun ArxivEntryDto.toDomain(): ArxivPaper {
    val cleanTitle = title.replace("\n", " ").trim().replace("\\s+".toRegex(), " ")
    val cleanSummary = summary.replace("\n", " ").trim()
    val arxivId = id.substringAfterLast("/")

    val domainAuthors = authors.map { name ->
        val stableId = Uuid.parse(java.util.UUID.nameUUIDFromBytes(name.toByteArray()).toString())
        PaperAuthor(stableId, name)
    }

    val domainCategories = categories.map { term ->
        val group = when {
            term.startsWith("cs.") -> MainField.COMPUTER_SCIENCE
            term.startsWith("math.") -> MainField.MATHEMATICS
            term.startsWith("q-bio.") -> MainField.QUANTITATIVE_BIOLOGY
            term.startsWith("q-fin.") -> MainField.QUANTITATIVE_FINANCE
            term.startsWith("stat.") -> MainField.STATISTICS
            term.startsWith("eess.") -> MainField.ELECTRICAL_ENGINEERING
            term.startsWith("econ.") -> MainField.ECONOMICS
            else -> MainField.PHYSICS
        }
        PaperCategory(term, term, group, "Sub-specialty $term")
    }

    return ArxivPaper(
        arxivId = arxivId,
        title = cleanTitle,
        authors = domainAuthors,
        summary = cleanSummary,
        journalReference = null,
        primaryCategory = domainCategories.firstOrNull() ?: PaperCategory("unknown", "Unknown", MainField.COMPUTER_SCIENCE, ""),
        allCategories = domainCategories,
        abstractUrl = id,
        pdfUrl = pdfUrl,
        htmlUrl = htmlUrl,
        publishedAt = Instant.parse(published),
        updatedAt = updated?.let { Instant.parse(it) },
        comment = null
    )
}