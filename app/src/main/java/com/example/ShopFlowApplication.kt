package com.example

import android.app.Application
import com.example.data.local.database.ShopFlowDatabase
import com.example.data.local.preferences.ShopFlowPreferences
import com.example.data.repository.ShopFlowRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ShopFlowApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy {
        ShopFlowDatabase.getDatabase(this, applicationScope)
    }

    val preferences by lazy {
        ShopFlowPreferences(this)
    }

    val repository by lazy {
        ShopFlowRepository(
            productDao = database.productDao(),
            saleDao = database.saleDao(),
            customerDao = database.customerDao(),
            supplierDao = database.supplierDao(),
            expenseDao = database.expenseDao(),
            preferences = preferences,
            inventoryItemDao = database.inventoryItemDao()
        )
    }
}
