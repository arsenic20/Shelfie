package com.example.shelfie.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.shelfie.core.common.AppError
import com.example.shelfie.core.common.AppResult
import com.example.shelfie.data.local.AppDatabase
import com.example.shelfie.data.remote.api.FakeStoreApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProductRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var db: AppDatabase
    private lateinit var repository: ProductRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FakeStoreApi::class.java)
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java)
            .allowMainThreadQueries().build()
        repository = ProductRepositoryImpl(api, db, db.productDao(), db.categoryDao(), UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        server.shutdown()
        db.close()
    }

    private fun enqueueSuccess(products: String = PRODUCTS_JSON, categories: String = """["bags","jewelery"]""") {
        server.enqueue(MockResponse().setBody(products))
        server.enqueue(MockResponse().setBody(categories))
    }

    @Test
    fun refreshStoresProductsAndCategoriesInRoom() = runBlocking {
        enqueueSuccess()

        assertEquals(AppResult.Success(Unit), repository.refreshProducts())

        assertEquals(listOf("Backpack", "Ring"), db.productDao().getAll().first().map { it.title })
        assertEquals(listOf("bags", "jewelery"), repository.observeCategories().first())
        assertEquals("Ring", repository.observeTopRated(1).first().single().title)
    }

    @Test
    fun searchEscapesWildcardsFromUserInput() = runBlocking {
        enqueueSuccess()
        repository.refreshProducts()

        assertEquals(listOf("Backpack"), repository.search("back").first().map { it.title })
        assertTrue(repository.search("%").first().isEmpty())
    }

    @Test
    fun failedRefreshKeepsCachedData() = runBlocking {
        enqueueSuccess()
        repository.refreshProducts()

        server.enqueue(MockResponse().setResponseCode(500))
        val result = repository.refreshProducts()

        assertTrue((result as AppResult.Failure).error is AppError.Http)
        assertEquals(2, db.productDao().getAll().first().size)
        assertEquals(2, repository.observeCategories().first().size)
    }

    @Test
    fun offlineRefreshReturnsNoNetworkAndKeepsCache() = runBlocking {
        enqueueSuccess()
        repository.refreshProducts()

        server.shutdown()
        assertEquals(AppResult.Failure(AppError.NoNetwork), repository.refreshProducts())
        assertEquals(2, db.productDao().getAll().first().size)
    }

    @Test
    fun failedCategoriesRequestDoesNotWriteProducts() = runBlocking {
        server.enqueue(MockResponse().setBody(PRODUCTS_JSON))
        server.enqueue(MockResponse().setResponseCode(500))

        assertTrue(repository.refreshProducts() is AppResult.Failure)
        assertTrue(db.productDao().getAll().first().isEmpty())
    }

    @Test
    fun malformedBodyMapsToUnexpected() = runBlocking {
        server.enqueue(MockResponse().setBody("not json"))

        val error = (repository.refreshProducts() as AppResult.Failure).error
        assertTrue(error is AppError.Unexpected)
    }

    @Test
    fun successfulRefreshRemovesProductsDeletedRemotely() = runBlocking {
        enqueueSuccess()
        repository.refreshProducts()
        enqueueSuccess(products = """[${ITEM_1}]""")
        repository.refreshProducts()

        assertEquals(listOf("Backpack"), db.productDao().getAll().first().map { it.title })
    }

    @Test
    fun getProductsByCategoryCallsCategoryEndpointAndDoesNotCache() = runBlocking {
        server.enqueue(MockResponse().setBody("[$ITEM_2]"))

        val result = repository.getProductsByCategory("men's clothing") as AppResult.Success
        assertEquals(listOf("Ring"), result.data.map { it.title })
        assertEquals("/products/category/men's%20clothing", server.takeRequest().path)
        assertTrue(db.productDao().getAll().first().isEmpty())
    }

    @Test
    fun getProductCallsDetailEndpointAndDoesNotCache() = runBlocking {
        server.enqueue(MockResponse().setBody(ITEM_1))

        val result = repository.getProduct(1) as AppResult.Success
        assertEquals("Backpack", result.data.title)
        assertEquals("/products/1", server.takeRequest().path)
        assertTrue(db.productDao().getAll().first().isEmpty())
    }

    @Test
    fun getProductMapsHttpErrors() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))
        val failure = repository.getProduct(1) as AppResult.Failure
        assertTrue(failure.error is AppError.Http)
    }

    private companion object {
        const val ITEM_1 = """{"id":1,"title":"Backpack","price":10.0,"description":"d","category":"bags","image":"i1","rating":{"rate":3.9,"count":120}}"""
        const val ITEM_2 = """{"id":2,"title":"Ring","price":99.0,"description":"d","category":"jewelery","image":"i2","rating":{"rate":4.8,"count":20}}"""
        const val PRODUCTS_JSON = "[$ITEM_1,$ITEM_2]"
    }
}
