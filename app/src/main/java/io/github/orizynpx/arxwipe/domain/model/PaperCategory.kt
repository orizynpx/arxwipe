package io.github.orizynpx.arxwipe.domain.model

data class PaperCategory(
    val categoryId: String,
    val displayName: String,
    val group: MainField,
    val subGroupDescription: String,
)

enum class MainField(val groupName: String) {
    PHYSICS("Physics"),
    COMPUTER_SCIENCE("Computer Science"),
    MATHEMATICS("Mathematics"),
    QUANTITATIVE_BIOLOGY("Quantitative Biology"),
    QUANTITATIVE_FINANCE("Quantitative Finance"),
    STATISTICS("Statistics"),
    ELECTRICAL_ENGINEERING("Electrical Engineering and Systems Science"),
    ECONOMICS("Economics")
}
