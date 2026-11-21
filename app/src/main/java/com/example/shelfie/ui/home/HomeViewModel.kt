package com.example.shelfie.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelfie.core.common.AppResult
import com.example.shelfie.core.common.UiState
import com.example.shelfie.domain.model.Product
import com.example.shelfie.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeData(
    val categories: List<String>,
    val products: List<Product>,
)

private const val TOP_RATED_COUNT = 5

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ProductRepository,
) : ViewModel() {

    private val refresh = MutableStateFlow<UiState<Unit>>(UiState.Loading)

    /** Cached data is always shown; the loader/error only appear while the cache is empty. */
    val state: StateFlow<UiState<HomeData>> = combine(
        repository.observeTopRated(TOP_RATED_COUNT),
        repository.observeCategories(),
        refresh,
    ) { products, categories, refreshState ->
        when {
            products.isNotEmpty() -> UiState.Success(HomeData(categories, products))
            refreshState is UiState.Error -> UiState.Error(refreshState.error)
            refreshState is UiState.Loading -> UiState.Loading
            else -> UiState.Success(HomeData(categories, products))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        load()
    }

    fun load() {
        refresh.value = UiState.Loading
        viewModelScope.launch {
            refresh.value = when (val r = repository.refreshProducts()) {
                is AppResult.Success -> UiState.Success(Unit)
                is AppResult.Failure -> UiState.Error(r.error)
            }
        }
    }
}
