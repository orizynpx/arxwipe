package io.github.orizynpx.arxwipe.data.remote.dto

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("link", namespace = "http://www.w3.org/2005/Atom", prefix = "")
data class ArxivLinkDto(
    @XmlElement(false) val href: String,
    @XmlElement(false) val rel: String? = null,
    @XmlElement(false) val title: String? = null,
    @XmlElement(false) val type: String? = null
)
