package io.github.orizynpx.arxwipe.data.remote

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface ArxivApiService {
    
    @GET("query")
    suspend fun getPapers(
        @Query("search_query") searchQuery: String,
        @Query("max_results") maxResults: Int,
        @Query("start") start: Int = 0,
        @Query("sortBy") sortBy: String? = "submittedDate",
        @Query("sortOrder") sortOrder: String? = "descending",
    ): ResponseBody
}
