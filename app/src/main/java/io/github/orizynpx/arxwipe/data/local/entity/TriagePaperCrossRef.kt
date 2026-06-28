package io.github.orizynpx.arxwipe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "triage_paper_cross_ref")
data class TriagePaperCrossRef(
    @PrimaryKey
    val arxivId: String
)
