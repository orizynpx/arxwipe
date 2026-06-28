package io.github.orizynpx.arxwipe.domain.model

import io.github.orizynpx.arxwipe.R

data class PaperCategory(
    val categoryId: String,
    val displayNameRes: Int,
    val group: MainField,
    val subGroupDescriptionRes: Int,
)

enum class MainField(val groupNameRes: Int) {
    PHYSICS(R.string.field_physics),
    COMPUTER_SCIENCE(R.string.field_computer_science),
    MATHEMATICS(R.string.field_mathematics),
    QUANTITATIVE_BIOLOGY(R.string.field_quantitative_biology),
    QUANTITATIVE_FINANCE(R.string.field_quantitative_finance),
    STATISTICS(R.string.field_statistics),
    ELECTRICAL_ENGINEERING(R.string.field_electrical_engineering),
    ECONOMICS(R.string.field_economics)
}
