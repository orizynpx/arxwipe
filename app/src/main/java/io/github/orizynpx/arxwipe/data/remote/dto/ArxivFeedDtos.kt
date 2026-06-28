package io.github.orizynpx.arxwipe.data.remote.dto

import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.ArxivTaxonomy
import io.github.orizynpx.arxwipe.domain.model.MainField
import io.github.orizynpx.arxwipe.domain.model.PaperAuthor
import io.github.orizynpx.arxwipe.domain.model.PaperCategory
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@XmlSerialName("feed", namespace = "http://www.w3.org/2005/Atom", prefix = "")
data class ArxivFeedDto(
    @XmlSerialName("entry", namespace = "http://www.w3.org/2005/Atom", prefix = "")
    val entries: List<ArxivEntryDto> = emptyList()
)

@Serializable
@XmlSerialName("entry", namespace = "http://www.w3.org/2005/Atom", prefix = "")
data class ArxivEntryDto(
    @XmlElement(true)
    @XmlSerialName("id", namespace = "http://www.w3.org/2005/Atom", prefix = "")
    val id: String = "",
    @XmlElement(true)
    @XmlSerialName("title", namespace = "http://www.w3.org/2005/Atom", prefix = "")
    val title: String = "",
    @XmlElement(true)
    @XmlSerialName("summary", namespace = "http://www.w3.org/2005/Atom", prefix = "")
    val summary: String = "",
    @XmlElement(true)
    @XmlSerialName("published", namespace = "http://www.w3.org/2005/Atom", prefix = "")
    val published: String = "",
    @XmlElement(true)
    @XmlSerialName("updated", namespace = "http://www.w3.org/2005/Atom", prefix = "")
    val updated: String? = null,
    @XmlElement(true)
    @XmlSerialName("author", namespace = "http://www.w3.org/2005/Atom", prefix = "")
    val authors: List<ArxivAuthorDto> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("category", namespace = "http://www.w3.org/2005/Atom", prefix = "")
    val categories: List<ArxivCategoryDto> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("link", namespace = "http://www.w3.org/2005/Atom", prefix = "")
    val links: List<ArxivLinkDto> = emptyList()
)

@Serializable
@XmlSerialName("author", namespace = "http://www.w3.org/2005/Atom", prefix = "")
data class ArxivAuthorDto(
    @XmlElement(true)
    @XmlSerialName("name", namespace = "http://www.w3.org/2005/Atom", prefix = "")
    val name: String = "Unknown Author"
)

@Serializable
@XmlSerialName("category", namespace = "http://www.w3.org/2005/Atom", prefix = "")
data class ArxivCategoryDto(
    @XmlElement(false) val term: String
)

@Serializable
@XmlSerialName("link", namespace = "http://www.w3.org/2005/Atom", prefix = "")
data class ArxivLinkDto(
    @XmlElement(false) val href: String,
    @XmlElement(false) val rel: String? = null,
    @XmlElement(false) val title: String? = null,
    @XmlElement(false) val type: String? = null
)

@OptIn(ExperimentalUuidApi::class)
fun ArxivEntryDto.toDomain(): ArxivPaper {
    val cleanTitle = title.replace("\n", " ").replace("\r", " ").trim().replace("\\s+".toRegex(), " ")
    val cleanSummary = summary.replace("\n", " ").replace("\r", " ").trim()
    
    
    val arxivId = id.substringAfterLast("/")

    val pdfUrl = links.find { it.title == "pdf" }?.href
    val htmlUrl = links.find { it.rel == "related" || it.type == "text/html" }?.href?.replace("/abs/", "/html/")

    val domainAuthors = authors.map { author ->
        
        val uuid = Uuid.parse(java.util.UUID.nameUUIDFromBytes(author.name.toByteArray()).toString())
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
