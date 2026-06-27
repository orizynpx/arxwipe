package io.github.orizynpx.arxwipe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "authors")
data class AuthorEntity(
    @PrimaryKey
    val authorId: String,
    val name: String
)
