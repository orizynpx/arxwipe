package io.github.orizynpx.arxwipe.domain.model

data class OnboardingPrefs(
    val selectedCategoryIds: List<String>,
    val batchSize: Int,
)
