package com.example.shelfie.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id")
    fun getAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY ratingRate DESC, ratingCount DESC LIMIT :limit")
    fun getTopRated(limit: Int): Flow<List<ProductEntity>>

    /** [query] must already have LIKE wildcards escaped with '\' (see [escapeLike]). SQLite LIKE is case-insensitive for ASCII. */
    @Query(
        "SELECT * FROM products WHERE " +
            "title LIKE '%' || :query || '%' ESCAPE '\\' OR " +
            "description LIKE '%' || :query || '%' ESCAPE '\\' OR " +
            "category LIKE '%' || :query || '%' ESCAPE '\\' " +
            "ORDER BY ratingRate DESC, ratingCount DESC"
    )
    fun search(query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(products: List<ProductEntity>)

    @Query("DELETE FROM products WHERE id NOT IN (:ids)")
    suspend fun deleteNotIn(ids: List<Int>)
}

@Dao
interface CategoryDao {
    @Query("SELECT name FROM categories ORDER BY name")
    fun getAll(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE name NOT IN (:names)")
    suspend fun deleteNotIn(names: List<String>)
}

fun escapeLike(input: String): String =
    input.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
