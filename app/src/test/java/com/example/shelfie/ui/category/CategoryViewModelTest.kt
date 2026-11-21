package com.example.shelfie.ui.category

import androidx.lifecycle.SavedStateHandle
import com.example.shelfie.core.common.AppError
import com.example.shelfie.core.common.AppResult
import com.example.shelfie.core.common.UiState
import com.example.shelfie.domain.model.Product
import com.example.shelfie.domain.model.Rating
import com.example.shelfie.domain.repository.ProductRepository
import com.example.shelfie.ui.components.prettifyCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CategoryViewModelTest {

    private fun p(id: Int, rate: Double, count: Int = 1) = Product(id, "P$id", 1.0, "d", "jewelery", "i", Rating(rate, count))

    private class FakeRepo(var result: AppResult<List<Product>>) : ProductRepository {
        var requested: String? = null
        var calls = 0
        override suspend fun getProductsByCategory(category: String): AppResult<List<Product>> {
            requested = category
            calls++
            return result
        }
        override fun observeTopRated(limit: Int) = flowOf(emptyList<Product>())
        override fun observeCategories() = flowOf(emptyList<String>())
        override fun search(query: String) = flowOf(emptyList<Product>())
        override suspend fun refreshProducts() = AppResult.Success(Unit)
        override suspend fun getProduct(id: Int): AppResult<Product> = AppResult.Failure(AppError.NoNetwork)
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(repo: FakeRepo, name: String = "jewelery") =
        CategoryViewModel(SavedStateHandle(mapOf("name" to name)), repo)

    @Test
    fun callsRepositoryWithRouteArgumentAndSplitsSortedByRatingDesc() = runTest {
        val repo = FakeRepo(AppResult.Success(listOf(p(1, 2.0), p(2, 4.9), p(3, 3.0), p(4, 4.0), p(5, 4.9, count = 50))))
        val data = (vm(repo).state.value as UiState.Success).data

        assertEquals(listOf(5, 2, 4), data.topRated.map { it.id })
        assertEquals(listOf(3, 1), data.more.map { it.id })
        assertEquals("jewelery", repo.requested)
    }

    @Test
    fun fewProductsAllGoToTopRated() = runTest {
        val data = (vm(FakeRepo(AppResult.Success(listOf(p(1, 1.0), p(2, 2.0))))).state.value as UiState.Success).data
        assertEquals(2, data.topRated.size)
        assertTrue(data.more.isEmpty())
    }

    @Test
    fun emptyCategoryIsReportedEmpty() = runTest {
        val data = (vm(FakeRepo(AppResult.Success(emptyList()))).state.value as UiState.Success).data
        assertTrue(data.isEmpty)
    }

    @Test
    fun errorWhenRepositoryFailsAndRetryReloads() = runTest {
        val repo = FakeRepo(AppResult.Failure(AppError.NoNetwork))
        val vm = vm(repo)
        assertEquals(UiState.Error(AppError.NoNetwork), vm.state.value)

        repo.result = AppResult.Success(listOf(p(1, 1.0)))
        vm.load()
        assertTrue(vm.state.value is UiState.Success)
        assertEquals(2, repo.calls)
    }

    @Test
    fun prettifiesCategoryNames() {
        assertEquals("Men's Clothing", "men's clothing".prettifyCategory())
        assertEquals("Jewelery", "jewelery".prettifyCategory())
        assertEquals("Electronics", "electronics".prettifyCategory())
    }
}
