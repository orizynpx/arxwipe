package io.github.orizynpx.arxwipe.data.remote.dto

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("feed", namespace = "http://www.w3.org/2005/Atom", prefix = "")
data class ArxivFeedDto(
    @XmlSerialName("entry", namespace = "http://www.w3.org/2005/Atom", prefix = "")
    val entries: List<ArxivEntryDto> = emptyList()
)
