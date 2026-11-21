package com.example.shelfie.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.shelfie.core.common.AppError
import com.example.shelfie.domain.model.Product
import com.example.shelfie.domain.repository.ProductRepository
import com.example.shelfie.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Results(val products: List<Product>) : SearchUiState
    data class Empty(val query: String) : SearchUiState
    data class Error(val error: AppError) : SearchUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ProductRepository,
) : ViewModel() {

    private val initialQuery = savedStateHandle.toRoute<Route.Search>().query.trim()

    private val _query = MutableStateFlow(initialQuery)
    /** Text shown in the editable search field. */
    val query: StateFlow<String> = _query.asStateFlow()

    private val submitted = MutableStateFlow(initialQuery)

    val state: StateFlow<SearchUiState> = submitted
        .flatMapLatest { q ->
            repository.search(q)
                .map<List<Product>, SearchUiState> { if (it.isEmpty()) SearchUiState.Empty(q) else SearchUiState.Results(it) }
                .onStart { emit(SearchUiState.Loading) }
                .catch { emit(SearchUiState.Error(AppError.Unexpected(it))) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState.Loading)

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onSearch() {
        val trimmed = _query.value.trim()
        if (trimmed.isEmpty()) return
        _query.value = trimmed
        submitted.value = trimmed
    }
}
