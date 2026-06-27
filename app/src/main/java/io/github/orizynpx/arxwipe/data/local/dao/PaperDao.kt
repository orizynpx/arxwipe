package io.github.orizynpx.arxwipe.data.local.dao

import androidx.room.*
import io.github.orizynpx.arxwipe.data.local.entity.AuthorEntity
import io.github.orizynpx.arxwipe.data.local.entity.PaperAuthorCrossRef
import io.github.orizynpx.arxwipe.data.local.entity.PaperEntity
import io.github.orizynpx.arxwipe.data.local.entity.PaperWithAuthors
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PaperDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPaper(paper: PaperEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPapers(papers: List<PaperEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertAuthors(authors: List<AuthorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPaperAuthorCrossRefs(crossRefs: List<PaperAuthorCrossRef>)

    @Transaction
    open suspend fun insertPapersWithAuthors(
        papers: List<PaperEntity>,
        authors: List<AuthorEntity>,
        crossRefs: List<PaperAuthorCrossRef>
    ) {
        insertPapers(papers)
        insertAuthors(authors)
        insertPaperAuthorCrossRefs(crossRefs)
    }

    @Transaction
    open suspend fun insertPaperWithAuthors(
        paper: PaperEntity,
        authors: List<AuthorEntity>,
        crossRefs: List<PaperAuthorCrossRef>
    ) {
        insertPaper(paper)
        insertAuthors(authors)
        insertPaperAuthorCrossRefs(crossRefs)
    }

    @Query("SELECT * FROM papers WHERE arxivId = :arxivId")
    abstract suspend fun getPaperById(arxivId: String): PaperEntity?

    @Transaction
    @Query("SELECT * FROM papers WHERE arxivId = :arxivId")
    abstract suspend fun getPaperWithAuthorsById(arxivId: String): PaperWithAuthors?

    @Transaction
    @Query("SELECT * FROM papers WHERE primaryCategoryId LIKE :categoryPattern OR allCategoryIds LIKE '%' || :categoryPattern || '%' LIMIT :limit")
    abstract suspend fun getPapersByCategory(categoryPattern: String, limit: Int): List<PaperWithAuthors>

    @Transaction
    @Query("SELECT * FROM papers LIMIT :limit")
    abstract suspend fun getAllPapers(limit: Int): List<PaperWithAuthors>

    @Transaction
    @Query("SELECT * FROM papers WHERE arxivId IN (SELECT arxivId FROM triage_paper_cross_ref)")
    abstract fun getActivePapers(): Flow<List<PaperWithAuthors>>

    @Transaction
    @Query("SELECT * FROM papers WHERE arxivId IN (SELECT arxivId FROM triage_paper_cross_ref)")
    abstract suspend fun getActivePapersList(): List<PaperWithAuthors>
}
