package com.example.ui.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.BusinessCategoriesEn
import com.example.localization.BusinessCategoriesUr
import com.example.ui.components.FuturisticButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.ShopBackgroundDark
import com.example.ui.theme.ShopBorderDark
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopSecondary

@Composable
fun OnboardingScreen(
    onComplete: (language: String, storeName: String, category: String) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf("ur") } // Default to Urdu or English
    var storeName by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var customCategory by remember { mutableStateOf("") }

    val isUrdu = selectedLanguage == "ur"
    val layoutDirection = if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
                .testTag("onboarding_screen")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // App Brand Icon
                Image(
                    painter = painterResource(id = R.drawable.smart_shop_app_icon),
                    contentDescription = "Smart Shop Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isUrdu) "سمارٹ شاپ" else "Smart Shop",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = if (isUrdu) "پاکستان کا اسمارٹ اور جدید ترین کاروباری نظام" else "Smart Business Management for Pakistan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShopCyanAccent,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 1. Language Selection Card
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = ShopCyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isUrdu) "زبان کا انتخاب کریں (Select Language)" else "Select Language / زبان کا انتخاب",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Urdu Option
                            val urduSelected = selectedLanguage == "ur"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (urduSelected) ShopEmeraldAccent.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .border(
                                        1.5.dp,
                                        if (urduSelected) ShopEmeraldAccent else MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedLanguage = "ur" }
                                    .padding(vertical = 14.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "اردو",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (urduSelected) ShopEmeraldAccent else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "پاکستان (Urdu)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // English Option
                            val englishSelected = selectedLanguage == "en"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (englishSelected) ShopPrimary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .border(
                                        1.5.dp,
                                        if (englishSelected) ShopPrimary else MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedLanguage = "en" }
                                    .padding(vertical = 14.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "English",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (englishSelected) ShopPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "International",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Business Category Selector
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = ShopEmeraldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isUrdu) "اپنے کاروبار کی قسم منتخب کریں" else "Select Your Business Category",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val categories = if (isUrdu) BusinessCategoriesUr else BusinessCategoriesEn
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories.indices.toList()) { index ->
                                val isSelected = selectedCategoryIndex == index
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) ShopEmeraldAccent else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { selectedCategoryIndex = index }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = categories[index],
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        val isOtherSelected = selectedCategoryIndex == categories.size - 1
                        if (isOtherSelected) {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = customCategory,
                                onValueChange = { customCategory = it },
                                placeholder = {
                                    Text(if (isUrdu) "اپنی کیٹیگری کا نام درج کریں (مثلاً: آٹو پارٹس، بیکری)" else "Enter custom category (e.g. Auto Parts, Bakery)")
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Store Name Input
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isUrdu) "دکان یا کاروبار کا نام" else "Business or Store Name",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            placeholder = {
                                Text(
                                    text = if (isUrdu) "مثلاً: مدینہ کیش اینڈ کیری" else "e.g., Madina Cash & Carry"
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShopEmeraldAccent,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 4. Get Started Button
                FuturisticButton(
                    text = if (isUrdu) "شروع کریں (Get Started)" else "Get Started (شروع کریں)",
                    icon = Icons.Default.ArrowForward,
                    onClick = {
                        val isOther = selectedCategoryIndex == BusinessCategoriesEn.size - 1
                        val finalCategory = if (isOther && customCategory.isNotBlank()) {
                            customCategory.trim()
                        } else {
                            BusinessCategoriesEn.getOrElse(selectedCategoryIndex) { "General Store" }
                        }
                        val finalName = storeName.trim().ifBlank {
                            if (isUrdu) "میرا کاروبار" else "My Karobar Store"
                        }
                        onComplete(selectedLanguage, finalName, finalCategory)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_onboarding_start"
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
