package io.github.orizynpx.arxwipe.data.remote.mapper

import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.data.remote.dto.ArxivEntryDto
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.ArxivTaxonomy
import io.github.orizynpx.arxwipe.domain.model.MainField
import io.github.orizynpx.arxwipe.domain.model.PaperAuthor
import io.github.orizynpx.arxwipe.domain.model.PaperCategory
import java.util.UUID
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun ArxivEntryDto.toDomain(): ArxivPaper {
    val cleanTitle = title.replace("\n", " ").replace("\r", " ").trim().replace("\\s+".toRegex(), " ")
    val cleanSummary = summary.replace("\n", " ").replace("\r", " ").trim()
    
    
    val arxivId = id.substringAfterLast("/")

    val pdfUrl = links.find { it.title == "pdf" }?.href
    val htmlUrl = links.find { it.rel == "related" || it.type == "text/html" }?.href?.replace("/abs/", "/html/")

    val domainAuthors = authors.map { author ->
        
        val uuid = Uuid.parse(UUID.nameUUIDFromBytes(author.name.toByteArray()).toString())
        PaperAuthor(
            authorId = uuid,
            name = author.name
        )
    }

    val domainCategories = categories.map {
        val term = it.term
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
        PaperCategory(
            categoryId = term,
            displayNameRes = ArxivTaxonomy.categories.find { it.categoryId == term }?.displayNameRes ?: R.string.no_results_found,
            group = group,
            subGroupDescriptionRes = ArxivTaxonomy.categories.find { it.categoryId == term }?.subGroupDescriptionRes ?: R.string.no_results_found
        )
    }

    return ArxivPaper(
        arxivId = arxivId,
        title = cleanTitle,
        authors = domainAuthors,
        summary = cleanSummary,
        journalReference = null,
        primaryCategory = domainCategories.firstOrNull() ?: PaperCategory("unknown", R.string.no_results_found, MainField.COMPUTER_SCIENCE, R.string.no_results_found),
        allCategories = domainCategories,
        abstractUrl = id,
        pdfUrl = pdfUrl,
        htmlUrl = htmlUrl,
        publishedAt = Instant.parse(published),
        updatedAt = updated?.let { Instant.parse(it) },
        comment = null
    )
}
