package io.github.orizynpx.arxwipe.domain.repository

import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.model.PaperCategory

interface PaperRepository {
    suspend fun getDiscoveryFeed(
        categoryId: String?,
        limit: Int = 20
    ): List<ArxivPaper>

    suspend fun getPaperById(paperId: String): ArxivPaper?
    suspend fun getAvailableCategories(): List<PaperCategory>
}