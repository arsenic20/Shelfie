package com.example.shelfie.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.example.shelfie.core.common.AppError
import com.example.shelfie.core.common.AppResult
import com.example.shelfie.domain.model.Product
import com.example.shelfie.domain.model.Rating
import com.example.shelfie.domain.repository.ProductRepository
import com.example.shelfie.ui.navigation.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.flow.launchIn
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SearchViewModelTest {

    private val product = Product(1, "Backpack", 1.0, "d", "bags", "i", Rating(4.0, 1))

    private class FakeRepo(val onSearch: (String) -> Flow<List<Product>>) : ProductRepository {
        val queries = mutableListOf<String>()
        override fun search(query: String): Flow<List<Product>> {
            queries += query
            return onSearch(query)
        }
        override fun observeTopRated(limit: Int) = flowOf(emptyList<Product>())
        override fun observeCategories() = flowOf(emptyList<String>())
        override suspend fun refreshProducts() = AppResult.Success(Unit)
        override suspend fun getProductsByCategory(category: String) = AppResult.Success(emptyList<Product>())
        override suspend fun getProduct(id: Int): AppResult<Product> = AppResult.Failure(com.example.shelfie.core.common.AppError.NoNetwork)
    }

    private fun TestScope.collect(vm: SearchViewModel) {
        vm.state.launchIn(backgroundScope + UnconfinedTestDispatcher(testScheduler))
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(repo: FakeRepo, query: String = "bag") =
        SearchViewModel(SavedStateHandle(mapOf("query" to query)), repo)

    @Test
    fun emitsResultsForInitialQuery() = runTest {
        val repo = FakeRepo { flowOf(listOf(product)) }
        val vm = vm(repo)
        collect(vm)

        assertEquals(SearchUiState.Results(listOf(product)), vm.state.value)
        assertEquals("bag", vm.query.value)
        assertEquals(listOf("bag"), repo.queries)
    }

    @Test
    fun emitsEmptyWithQueryWhenNoMatch() = runTest {
        val vm = vm(FakeRepo { flowOf(emptyList()) }, "xyz")
        collect(vm)

        assertEquals(SearchUiState.Empty("xyz"), vm.state.value)
    }

    @Test
    fun emitsErrorWhenRepositoryFails() = runTest {
        val vm = vm(FakeRepo { flow { throw IllegalStateException("boom") } })
        collect(vm)

        assertEquals(true, (vm.state.value as SearchUiState.Error).error is AppError.Unexpected)
    }

    @Test
    fun searchTrimsInputAndIgnoresEmpty() = runTest {
        val repo = FakeRepo { flowOf(listOf(product)) }
        val vm = vm(repo)
        collect(vm)

        vm.onQueryChange("   ")
        vm.onSearch()
        assertEquals(listOf("bag"), repo.queries)

        vm.onQueryChange("  ring  ")
        vm.onSearch()
        assertEquals("ring", vm.query.value)
        assertEquals(listOf("bag", "ring"), repo.queries)
    }

    @Test
    fun typingDoesNotTriggerSearch() = runTest {
        val repo = FakeRepo { flowOf(listOf(product)) }
        val vm = vm(repo)
        collect(vm)

        vm.onQueryChange("r")
        vm.onQueryChange("ri")

        assertEquals(listOf("bag"), repo.queries)
    }
}
