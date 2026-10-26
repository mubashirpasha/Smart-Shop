package com.example.ui.components

import androidx.compose.runtime.Composable
import com.example.data.local.entity.ProductEntity

@Composable
fun QuickAddProductDialog(
    onDismiss: () -> Unit,
    onProductAdded: (ProductEntity) -> Unit
) {
    ProductFormDialog(
        initialProduct = null,
        existingCategories = listOf("Hardware", "Wearables", "Peripherals", "Accessories", "Ambient", "Displays", "Gear"),
        onDismiss = onDismiss,
        onSaveProduct = onProductAdded
    )
}
