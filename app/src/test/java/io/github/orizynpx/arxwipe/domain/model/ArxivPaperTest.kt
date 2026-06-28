package io.github.orizynpx.arxwipe.domain.model

import io.mockk.mockk
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ArxivPaperTest {

    @OptIn(ExperimentalUuidApi::class)
    private fun createPaper(
        authors: List<PaperAuthor> = emptyList(),
        pdfUrl: String? = null,
        htmlUrl: String? = null
    ) = ArxivPaper(
        arxivId = "2401.00001",
        title = "Test Paper",
        authors = authors,
        summary = "Summary",
        journalReference = null,
        primaryCategory = mockk(),
        allCategories = emptyList(),
        abstractUrl = null,
        pdfUrl = pdfUrl,
        htmlUrl = htmlUrl,
        publishedAt = Instant.fromEpochMilliseconds(0),
        updatedAt = null,
        comment = null
    )

    @Test
    fun `hasPdf returns true when pdfUrl is not null or blank`() {
        assertTrue(createPaper(pdfUrl = "http://example.com/pdf").hasPdf)
    }

    @Test
    fun `hasPdf returns false when pdfUrl is null`() {
        assertFalse(createPaper(pdfUrl = null).hasPdf)
    }

    @Test
    fun `hasPdf returns false when pdfUrl is blank`() {
        assertFalse(createPaper(pdfUrl = "  ").hasPdf)
    }

    @Test
    fun `hasHtml returns true when htmlUrl is not null or blank`() {
        assertTrue(createPaper(htmlUrl = "http://example.com/html").hasHtml)
    }

    @Test
    fun `hasHtml returns false when htmlUrl is null`() {
        assertFalse(createPaper(htmlUrl = null).hasHtml)
    }

    @Test
    fun `hasHtml returns false when htmlUrl is blank`() {
        assertFalse(createPaper(htmlUrl = "  ").hasHtml)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `formattedAuthors returns comma separated names`() {
        val authors = listOf(
            PaperAuthor(Uuid.random(), "Author One"),
            PaperAuthor(Uuid.random(), "Author Two"),
            PaperAuthor(Uuid.random(), "Author Three")
        )
        val paper = createPaper(authors = authors)
        assertEquals("Author One, Author Two, Author Three", paper.formattedAuthors)
    }

    @Test
    fun `formattedAuthors returns single name for one author`() {
        @OptIn(ExperimentalUuidApi::class)
        val authors = listOf(PaperAuthor(Uuid.random(), "Only One"))
        val paper = createPaper(authors = authors)
        assertEquals("Only One", paper.formattedAuthors)
    }

    @Test
    fun `formattedAuthors returns empty string for no authors`() {
        val paper = createPaper(authors = emptyList())
        assertEquals("", paper.formattedAuthors)
    }
}
