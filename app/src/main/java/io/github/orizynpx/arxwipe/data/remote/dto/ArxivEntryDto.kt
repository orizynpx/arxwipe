package io.github.orizynpx.arxwipe.data.remote.dto

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

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
