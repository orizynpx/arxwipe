package io.github.orizynpx.arxwipe.data.remote.dto

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("category", namespace = "http://www.w3.org/2005/Atom", prefix = "")
data class ArxivCategoryDto(
    @XmlElement(false) val term: String
)
