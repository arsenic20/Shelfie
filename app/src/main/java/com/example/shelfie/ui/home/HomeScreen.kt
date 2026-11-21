package com.example.shelfie.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.shelfie.ui.components.ErrorView
import com.example.shelfie.ui.components.MenuButton
import com.example.shelfie.core.common.UiState
import com.example.shelfie.ui.components.ProductCard
import com.example.shelfie.ui.components.SearchBar
import com.example.shelfie.ui.components.prettifyCategory
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (Int) -> Unit,
    onSearch: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val categories = (state as? UiState.Success)?.data?.categories.orEmpty()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Categories", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                categories.forEach { category ->
                    NavigationDrawerItem(
                        label = { Text(category.prettifyCategory()) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onCategoryClick(category)
                        },
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Welcome to Shelfie") },
                    navigationIcon = {
                        MenuButton { scope.launch { drawerState.open() } }
                    },
                )
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (val s = state) {
                    UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    is UiState.Error -> ErrorView(s.error, viewModel::load, Modifier.align(Alignment.Center))
                    is UiState.Success -> HomeContent(s.data, onProductClick, onSearch)
                }
            }
        }
    }
}

@Composable
private fun HomeContent(data: HomeData, onProductClick: (Int) -> Unit, onSearch: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { query.trim().takeIf { it.isNotEmpty() }?.let(onSearch) },
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text("Top rated", style = MaterialTheme.typography.titleMedium)
        }
        items(data.products, key = { it.id }) { ProductCard(it, onProductClick) }
    }
}
