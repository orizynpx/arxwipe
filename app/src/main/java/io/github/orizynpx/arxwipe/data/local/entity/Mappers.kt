package io.github.orizynpx.arxwipe.data.local.entity

import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.ArxivTaxonomy
import io.github.orizynpx.arxwipe.domain.model.MainField
import io.github.orizynpx.arxwipe.domain.model.PaperAuthor
import io.github.orizynpx.arxwipe.domain.model.PaperCategory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun PaperWithAuthors.toDomain(): ArxivPaper {
    return ArxivPaper(
        arxivId = paper.arxivId,
        title = paper.title,
        authors = authors.map { PaperAuthor(authorId = Uuid.parse(it.authorId), name = it.name) },
        summary = paper.summary,
        journalReference = paper.journalReference,
        primaryCategory = PaperCategory(
            categoryId = paper.primaryCategoryId,
            displayNameRes = ArxivTaxonomy.categories.find { it.categoryId == paper.primaryCategoryId }?.displayNameRes ?: R.string.no_results_found,
            group = MainField.valueOf(paper.primaryCategoryGroup),
            subGroupDescriptionRes = ArxivTaxonomy.categories.find { it.categoryId == paper.primaryCategoryId }?.subGroupDescriptionRes ?: R.string.no_results_found
        ),
        allCategories = paper.allCategoryIds.split(",")
            .filter { it.isNotBlank() }
            .mapNotNull { id ->
                ArxivTaxonomy.categories.find { it.categoryId == id }
            },
        abstractUrl = paper.abstractUrl,
        pdfUrl = paper.pdfUrl,
        htmlUrl = paper.htmlUrl?.replace("/abs/", "/html/"),
        publishedAt = paper.publishedAt,
        updatedAt = paper.updatedAt,
        comment = paper.comment
    )
}

fun ArxivPaper.toEntity(): PaperEntity {
    return PaperEntity(
        arxivId = arxivId,
        title = title,
        summary = summary,
        journalReference = journalReference,
        primaryCategoryId = primaryCategory.categoryId,
        primaryCategoryDisplayName = "", 
        primaryCategoryGroup = primaryCategory.group.name,
        primaryCategorySubGroupDescription = "", 
        allCategoryIds = allCategories.joinToString(",") { it.categoryId },
        publishedAt = publishedAt,
        updatedAt = updatedAt,
        comment = comment,
        abstractUrl = abstractUrl,
        pdfUrl = pdfUrl,
        htmlUrl = htmlUrl
    )
}
