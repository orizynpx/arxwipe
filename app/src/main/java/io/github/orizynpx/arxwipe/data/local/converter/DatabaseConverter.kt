package io.github.orizynpx.arxwipe.data.local.converter

import androidx.room.TypeConverter
import io.github.orizynpx.arxwipe.data.local.entity.AuthorDb
import io.github.orizynpx.arxwipe.data.local.entity.CategoryDb
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DatabaseConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromAuthors(authors: List<AuthorDb>): String = json.encodeToString(authors)

    @TypeConverter
    fun toAuthors(value: String): List<AuthorDb> = json.decodeFromString(value)

    @TypeConverter
    fun fromCategories(categories: List<CategoryDb>): String = json.encodeToString(categories)

    @TypeConverter
    fun toCategories(value: String): List<CategoryDb> = json.decodeFromString(value)

    @TypeConverter
    fun fromCategory(category: CategoryDb): String = json.encodeToString(category)

    @TypeConverter
    fun toCategory(value: String): CategoryDb = json.decodeFromString(value)

    @TypeConverter
    fun fromStringList(list: List<String>): String = json.encodeToString(list)

    @TypeConverter
    fun toStringList(value: String): List<String> = json.decodeFromString(value)
}