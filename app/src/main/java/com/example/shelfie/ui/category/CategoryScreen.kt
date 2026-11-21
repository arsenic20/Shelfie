package com.example.shelfie.ui.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shelfie.ui.components.BackButton
import com.example.shelfie.ui.components.ErrorView
import com.example.shelfie.core.common.UiState
import com.example.shelfie.ui.components.ProductCard
import com.example.shelfie.ui.components.prettifyCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onProductClick: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(viewModel.category.prettifyCategory()) },
                navigationIcon = {
                    BackButton(onBack)
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Error -> ErrorView(s.error, viewModel::load, Modifier.align(Alignment.Center))
                is UiState.Success -> if (s.data.isEmpty) {
                    Text("No products in this category.", Modifier.align(Alignment.Center))
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text("Top rated", style = MaterialTheme.typography.titleMedium)
                        }
                        items(s.data.topRated, key = { it.id }) { ProductCard(it, onProductClick) }
                        if (s.data.more.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text("More in this category", style = MaterialTheme.typography.titleMedium)
                            }
                            items(s.data.more, key = { it.id }) { ProductCard(it, onProductClick) }
                        }
                    }
                }
            }
        }
    }
}
