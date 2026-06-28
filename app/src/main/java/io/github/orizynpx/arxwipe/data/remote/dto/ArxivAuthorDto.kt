package io.github.orizynpx.arxwipe.data.remote.dto

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("author", namespace = "http://www.w3.org/2005/Atom", prefix = "")
data class ArxivAuthorDto(
    @XmlElement(true)
    @XmlSerialName("name", namespace = "http://www.w3.org/2005/Atom", prefix = "")
    val name: String = "Unknown Author"
)
