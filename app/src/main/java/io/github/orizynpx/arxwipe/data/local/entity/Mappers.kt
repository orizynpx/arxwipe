package io.github.orizynpx.arxwipe.data.local.entity

import io.github.orizynpx.arxwipe.domain.model.*
import kotlinx.serialization.json.Json
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val json = Json { ignoreUnknownKeys = true }

fun PaperEntity.toDomain(): ArxivPaper {
    val authors: List<AuthorDb> = json.decodeFromString(authorsJson)
    val categories: List<CategoryDb> = json.decodeFromString(categoriesJson)
    val primary: CategoryDb = json.decodeFromString(primaryCategoryJson)

    return ArxivPaper(
        arxivId = arxivId,
        title = title,
        summary = summary,
        journalReference = journalReference,
        abstractUrl = abstractUrl,
        pdfUrl = pdfUrl,
        htmlUrl = htmlUrl,
        publishedAt = Instant.fromEpochMilliseconds(publishedAtMillis),
        updatedAt = updatedAtMillis?.let { Instant.fromEpochMilliseconds(it) },
        comment = comment,
        authors = authors.map { PaperAuthor(Uuid.parse(it.id), it.name) },
        allCategories = categories.map { CategoryDb ->
            PaperCategory(
                CategoryDb.id,
                CategoryDb.displayName,
                MainField.valueOf(CategoryDb.group),
                CategoryDb.desc
            )
        },
        primaryCategory = PaperCategory(
            primary.id,
            primary.displayName,
            MainField.valueOf(primary.group),
            primary.desc
        )
    )
}

fun ArxivPaper.toEntity(): PaperEntity {
    val authorsDb = authors.map { AuthorDb(it.authorId.toString(), it.name) }
    val categoriesDb = allCategories.map { CategoryDb(it.categoryId, it.displayName, it.group.name, it.subGroupDescription) }
    val primaryDb = CategoryDb(primaryCategory.categoryId, primaryCategory.displayName, primaryCategory.group.name, primaryCategory.subGroupDescription)

    return PaperEntity(
        arxivId = arxivId,
        title = title,
        summary = summary,
        journalReference = journalReference,
        abstractUrl = abstractUrl,
        pdfUrl = pdfUrl,
        htmlUrl = htmlUrl,
        publishedAtMillis = publishedAt.toEpochMilliseconds(),
        updatedAtMillis = updatedAt?.toEpochMilliseconds(),
        comment = comment,
        authorsJson = json.encodeToString(authorsDb),
        categoriesJson = json.encodeToString(categoriesDb),
        primaryCategoryJson = json.encodeToString(primaryDb)
    )
}