package com.example.shelfie.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.shelfie.domain.model.Product

@Composable
fun ProductCard(product: Product, onClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = { onClick(product.id) }, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().height(140.dp),
            )
            Text(product.title, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
            Text("$${product.price}", style = MaterialTheme.typography.bodyMedium)
            Text("★ ${product.rating.rate} (${product.rating.count})", style = MaterialTheme.typography.bodySmall)
        }
    }
}
