package com.example.shelfie.domain.repository

import com.example.shelfie.core.common.AppResult
import com.example.shelfie.domain.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Only the home top-rated list, the category list and search are served from the local cache.
 * Category products and product details are always fetched from the remote source.
 */
interface ProductRepository {
    fun observeTopRated(limit: Int = 5): Flow<List<Product>>
    fun observeCategories(): Flow<List<String>>

    /** Searches the cached products by title, description and category; wildcards in [query] are escaped. */
    fun search(query: String): Flow<List<Product>>

    /** Fetches products and categories into the cache; on failure the cache is left untouched. */
    suspend fun refreshProducts(): AppResult<Unit>

    /** Network call (no caching): products of one category as returned by the API. */
    suspend fun getProductsByCategory(category: String): AppResult<List<Product>>

    /** Network call (no caching): a single product. */
    suspend fun getProduct(id: Int): AppResult<Product>
}
