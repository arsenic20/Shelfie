package com.example.shelfie.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.shelfie.ui.components.BackButton
import com.example.shelfie.ui.components.ErrorView
import com.example.shelfie.ui.components.prettifyCategory
import com.example.shelfie.core.common.UiState
import com.example.shelfie.domain.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(onBack: () -> Unit, onEnquire: (Int, String) -> Unit, viewModel: ProductDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Product details") },
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
                is UiState.Success -> DetailContent(s.data, onEnquire)
            }
        }
    }
}

@Composable
private fun DetailContent(product: Product, onEnquire: (Int, String) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().height(320.dp),
        )
        Text(product.title, style = MaterialTheme.typography.headlineSmall)
        Text("$${product.price}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Text("★ ${product.rating.rate} (${product.rating.count} reviews)", style = MaterialTheme.typography.bodyMedium)
        Text(product.category.prettifyCategory(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
        Text("Description", style = MaterialTheme.typography.titleMedium)
        Text(product.description, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = { onEnquire(product.id, product.title) }, modifier = Modifier.fillMaxWidth()) { Text("Enquire") }
    }
}
