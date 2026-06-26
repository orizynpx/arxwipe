package io.github.orizynpx.arxwipe.ui.util

import android.text.Html
import android.text.Spanned

fun String.formatAbstract(): Spanned {
    var cleaned = this
        .replace("\\mathcal{O}", "O")
        .replace("\\times", " × ")
        .replace("\\approx", " ≈ ")
        .replace("\\neq", " ≠ ")
        .replace("\\le", " ≤ ")
        .replace("\\ge", " ≥ ")
        .replace("\\infty", "∞")

    cleaned = cleaned.replace(Regex("\\$\\$?"), "")

    val finalString = cleaned.replace("\\s+".toRegex(), " ").trim()

    return Html.fromHtml(finalString, Html.FROM_HTML_MODE_COMPACT)
}
