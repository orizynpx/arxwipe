package io.github.orizynpx.arxwipe.domain.model

import io.github.orizynpx.arxwipe.R

data class FilterSubcategory(
    val label: String? = null,
    val labelRes: Int? = null,
    val codes: List<String>
)

data class FilterCategory(
    val name: String? = null,
    val nameRes: Int? = null,
    val subcategories: List<FilterSubcategory>
)

object SearchFilterCategories {

    val groups: List<FilterCategory> = buildList {
        add(fromTaxonomy(MainField.COMPUTER_SCIENCE))
        add(fromTaxonomy(MainField.ECONOMICS))
        add(fromTaxonomy(MainField.ELECTRICAL_ENGINEERING))
        add(fromTaxonomy(MainField.MATHEMATICS))
        add(physicsGroup())
        add(fromTaxonomy(MainField.QUANTITATIVE_BIOLOGY))
        add(fromTaxonomy(MainField.QUANTITATIVE_FINANCE))
        add(fromTaxonomy(MainField.STATISTICS))
    }

    private fun fromTaxonomy(field: MainField): FilterCategory {
        val subs = ArxivTaxonomy.getByCategoryGroup(field)
            .filterNot { it.categoryId.endsWith(".*") }
            .map { FilterSubcategory(codes = listOf(it.categoryId), labelRes = it.displayNameRes) }
        return FilterCategory(nameRes = field.groupNameRes, subcategories = subs)
    }

    private fun physicsGroup(): FilterCategory = FilterCategory(
        nameRes = MainField.PHYSICS.groupNameRes,
        subcategories = listOf(
            FilterSubcategory(codes = listOf("physics.*"), labelRes = R.string.cat_physics_all_name),
            FilterSubcategory(codes = listOf("astro-ph.*"), labelRes = R.string.cat_astro_ph_all_name),
            FilterSubcategory(codes = listOf("cond-mat.*"), labelRes = R.string.cat_cond_mat_all_name),
            FilterSubcategory(codes = listOf("nlin.*"), labelRes = R.string.cat_nlin_all_name),
            FilterSubcategory(codes = listOf("gr-qc"), labelRes = R.string.cat_gr_qc_name),
            FilterSubcategory(
                label = "hep-* (High Energy Physics)",
                codes = listOf("hep-ex", "hep-lat", "hep-ph", "hep-th")
            ),
            FilterSubcategory(label = "nucl-* (Nuclear)", codes = listOf("nucl-ex", "nucl-th")),
            FilterSubcategory(codes = listOf("math-ph"), labelRes = R.string.cat_math_ph_name),
            FilterSubcategory(codes = listOf("quant-ph"), labelRes = R.string.cat_quant_ph_name)
        )
    )

    val allSubcategories: List<FilterSubcategory> get() = groups.flatMap { it.subcategories }
}
