package io.github.orizynpx.arxwipe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "papers")
data class PaperEntity(
    @PrimaryKey
    val arxivId: String,
    val title: String,
    val summary: String,
    val journalReference: String?,
    
    
    val primaryCategoryId: String,
    val primaryCategoryDisplayName: String,
    val primaryCategoryGroup: String, 
    val primaryCategorySubGroupDescription: String,
    
    val allCategoryIds: String, 

    val publishedAt: Instant,
    val updatedAt: Instant?,
    val comment: String?,

    
    val abstractUrl: String?,
    val pdfUrl: String?,
    val htmlUrl: String?
)
