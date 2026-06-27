package io.github.orizynpx.arxwipe.domain.repository

import kotlinx.coroutines.flow.Flow


interface SearchHistoryRepository {
    fun getHistory(): Flow<List<String>>
    suspend fun record(query: String)
    suspend fun delete(query: String)
    suspend fun clear()
}
