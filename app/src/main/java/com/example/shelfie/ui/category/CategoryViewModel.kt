package com.example.shelfie.ui.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.shelfie.core.common.AppResult
import com.example.shelfie.core.common.UiState
import com.example.shelfie.domain.model.Product
import com.example.shelfie.domain.repository.ProductRepository
import com.example.shelfie.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryData(
    val topRated: List<Product>,
    val more: List<Product>,
) {
    val isEmpty get() = topRated.isEmpty() && more.isEmpty()
}

const val CATEGORY_TOP_RATED_COUNT = 3

/** Category products come straight from GET /products/category/{name} (not cached). */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ProductRepository,
) : ViewModel() {

    val category: String = savedStateHandle.toRoute<Route.Category>().name

    private val _state = MutableStateFlow<UiState<CategoryData>>(UiState.Loading)
    val state: StateFlow<UiState<CategoryData>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = when (val r = repository.getProductsByCategory(category)) {
                is AppResult.Failure -> UiState.Error(r.error)
                is AppResult.Success -> {
                    val sorted = r.data.sortedWith(
                        compareByDescending<Product> { it.rating.rate }.thenByDescending { it.rating.count },
                    )
                    UiState.Success(CategoryData(sorted.take(CATEGORY_TOP_RATED_COUNT), sorted.drop(CATEGORY_TOP_RATED_COUNT)))
                }
            }
        }
    }
}
