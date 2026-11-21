package com.example.shelfie.data.repository

import androidx.room.withTransaction
import com.example.shelfie.core.common.AppResult
import com.example.shelfie.core.common.IoDispatcher
import com.example.shelfie.core.common.map
import com.example.shelfie.core.network.safeApiCall
import com.example.shelfie.data.local.AppDatabase
import com.example.shelfie.data.local.CategoryDao
import com.example.shelfie.data.local.CategoryEntity
import com.example.shelfie.data.local.ProductDao
import com.example.shelfie.data.local.escapeLike
import com.example.shelfie.data.mapper.toDomain
import com.example.shelfie.data.mapper.toEntity
import com.example.shelfie.data.remote.api.FakeStoreApi
import com.example.shelfie.domain.model.Product
import com.example.shelfie.domain.repository.ProductRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ProductRepositoryImpl @Inject constructor(
    private val api: FakeStoreApi,
    private val db: AppDatabase,
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ProductRepository {

    override fun observeTopRated(limit: Int): Flow<List<Product>> =
        productDao.getTopRated(limit).map { l -> l.map { it.toDomain() } }

    override fun observeCategories(): Flow<List<String>> = categoryDao.getAll()

    override fun search(query: String): Flow<List<Product>> =
        productDao.search(escapeLike(query)).map { l -> l.map { it.toDomain() } }

    override suspend fun refreshProducts(): AppResult<Unit> = withContext(ioDispatcher) {
        // Fetch everything first so a failure never touches the cache.
        val products = when (val r = safeApiCall { api.getProducts() }) {
            is AppResult.Success -> r.data
            is AppResult.Failure -> return@withContext r
        }
        val categories = when (val r = safeApiCall { api.getCategories() }) {
            is AppResult.Success -> r.data
            is AppResult.Failure -> return@withContext r
        }
        safeApiCall {
            db.withTransaction {
                if (products.isNotEmpty()) {
                    productDao.upsertAll(products.map { it.toEntity() })
                    productDao.deleteNotIn(products.map { it.id })
                }
                if (categories.isNotEmpty()) {
                    categoryDao.upsertAll(categories.map { CategoryEntity(it) })
                    categoryDao.deleteNotIn(categories)
                }
            }
        }
    }

    override suspend fun getProductsByCategory(category: String): AppResult<List<Product>> = withContext(ioDispatcher) {
        safeApiCall { api.getProductsByCategory(category) }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getProduct(id: Int): AppResult<Product> = withContext(ioDispatcher) {
        safeApiCall { api.getProduct(id) }.map { it.toDomain() }
    }
}
