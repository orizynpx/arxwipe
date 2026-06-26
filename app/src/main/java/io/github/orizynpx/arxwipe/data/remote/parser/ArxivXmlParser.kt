package io.github.orizynpx.arxwipe.data.remote.parser

import android.util.Xml
import io.github.orizynpx.arxwipe.data.remote.dto.ArxivEntryDto
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

object ArxivXmlParser {
    private val ns: String? = null

    fun parse(inputStream: InputStream): List<ArxivEntryDto> {
        inputStream.use { stream ->
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(stream, null)
            parser.nextTag()
            return readFeed(parser)
        }
    }

    private fun readFeed(parser: XmlPullParser): List<ArxivEntryDto> {
        val entries = mutableListOf<ArxivEntryDto>()
        parser.require(XmlPullParser.START_TAG, ns, "feed")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "entry") {
                entries.add(readEntry(parser))
            } else {
                skip(parser)
            }
        }
        return entries
    }

    private fun readEntry(parser: XmlPullParser): ArxivEntryDto {
        parser.require(XmlPullParser.START_TAG, ns, "entry")
        var id = ""
        var title = ""
        var summary = ""
        var published = ""
        var updated: String? = null
        val authors = mutableListOf<String>()
        val categories = mutableListOf<String>()
        var pdfUrl: String? = null
        var htmlUrl: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "id" -> id = readText(parser, "id")
                "title" -> title = readText(parser, "title")
                "summary" -> summary = readText(parser, "summary")
                "published" -> published = readText(parser, "published")
                "updated" -> updated = readText(parser, "updated")
                "author" -> authors.add(readAuthor(parser))
                "category" -> categories.add(readCategory(parser))
                "link" -> {
                    val (rel, href) = readLink(parser)
                    if (rel == "alternate") htmlUrl = href
                    if (title == "pdf" || rel == "related") pdfUrl = href
                }
                else -> skip(parser)
            }
        }
        return ArxivEntryDto(
            id,
            title,
            summary,
            published,
            updated,
            authors,
            categories,
            pdfUrl,
            htmlUrl
        )
    }

    private fun readAuthor(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "author")
        var name = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "name") {
                name = readText(parser, "name")
            } else {
                skip(parser)
            }
        }
        return name
    }

    private fun readCategory(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "category")
        val term = parser.getAttributeValue(null, "term") ?: ""
        parser.nextTag()
        return term
    }

    private fun readLink(parser: XmlPullParser): Pair<String, String> {
        parser.require(XmlPullParser.START_TAG, ns, "link")
        val rel = parser.getAttributeValue(null, "rel") ?: ""
        val href = parser.getAttributeValue(null, "href") ?: ""
        parser.nextTag()
        return Pair(rel, href)
    }

    private fun readText(parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, ns, tag)
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}