package com.example.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopRoseAccent
import com.example.ui.theme.ShopSecondary

data class ProductPresetVisual(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val accentColor: Color
)

val ProductVisualPresets = listOf(
    ProductPresetVisual("preset_workstation", "Workstation", Icons.Default.Computer, ShopPrimary),
    ProductPresetVisual("preset_hud", "Vision HUD", Icons.Default.Visibility, ShopCyanAccent),
    ProductPresetVisual("preset_controller", "Controller", Icons.Default.VideogameAsset, ShopSecondary),
    ProductPresetVisual("preset_cooling", "Cooling Core", Icons.Default.DeviceHub, ShopCyanAccent),
    ProductPresetVisual("preset_neon", "Neon Photon", Icons.Default.Bolt, Color(0xFFFF0055)),
    ProductPresetVisual("preset_display", "4K Display", Icons.Default.Tv, ShopPrimary),
    ProductPresetVisual("preset_titanium", "Titanium EDC", Icons.Default.Hardware, Color(0xFFC0C0C0)),
    ProductPresetVisual("preset_chip", "Neural Chip", Icons.Default.Memory, ShopEmeraldAccent),
    ProductPresetVisual("preset_audio", "Bio Headset", Icons.Default.Headset, ShopSecondary),
    ProductPresetVisual("preset_shield", "Nano Shield", Icons.Default.Security, ShopRoseAccent)
)

@Composable
fun ProductVisualBadge(
    imageUri: String,
    category: String,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp
) {
    val context = LocalContext.current

    // If it's a content URI picked from Android Photo Picker, attempt to load bitmap
    val bitmap = remember(imageUri) {
        if (imageUri.startsWith("content://") || imageUri.startsWith("file://")) {
            runCatching {
                val uri = Uri.parse(imageUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()
        } else null
    }

    val matchedPreset = remember(imageUri, category) {
        ProductVisualPresets.find { it.id == imageUri }
            ?: when {
                category.contains("Hardware", ignoreCase = true) -> ProductVisualPresets[0]
                category.contains("Wearables", ignoreCase = true) -> ProductVisualPresets[1]
                category.contains("Peripherals", ignoreCase = true) -> ProductVisualPresets[2]
                category.contains("Accessories", ignoreCase = true) -> ProductVisualPresets[3]
                category.contains("Ambient", ignoreCase = true) -> ProductVisualPresets[4]
                category.contains("Display", ignoreCase = true) -> ProductVisualPresets[5]
                category.contains("Gear", ignoreCase = true) -> ProductVisualPresets[6]
                else -> ProductVisualPresets[7]
            }
    }

    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        matchedPreset.accentColor.copy(alpha = 0.28f),
                        matchedPreset.accentColor.copy(alpha = 0.10f)
                    )
                )
            )
            .border(1.dp, matchedPreset.accentColor.copy(alpha = 0.35f), shape),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Product Image",
                modifier = Modifier
                    .size(size)
                    .clip(shape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = matchedPreset.icon,
                contentDescription = matchedPreset.label,
                tint = matchedPreset.accentColor,
                modifier = Modifier.size(size * 0.52f)
            )
        }
    }
}
