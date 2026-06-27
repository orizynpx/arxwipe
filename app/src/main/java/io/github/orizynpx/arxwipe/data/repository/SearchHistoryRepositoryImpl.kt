package io.github.orizynpx.arxwipe.data.repository

import io.github.orizynpx.arxwipe.data.local.dao.SearchHistoryDao
import io.github.orizynpx.arxwipe.data.local.entity.SearchHistoryEntity
import io.github.orizynpx.arxwipe.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchHistoryRepositoryImpl @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao
) : SearchHistoryRepository {

    override fun getHistory(): Flow<List<String>> =
        searchHistoryDao.observeHistory().map { entries -> entries.map { it.query } }

    override suspend fun record(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        searchHistoryDao.upsert(SearchHistoryEntity(trimmed, System.currentTimeMillis()))
    }

    override suspend fun delete(query: String) {
        searchHistoryDao.delete(query)
    }

    override suspend fun clear() {
        searchHistoryDao.clear()
    }
}
