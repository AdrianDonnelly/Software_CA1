package com.example.carparts.util

internal fun Map<String, String>.getFirstNonBlank(vararg keys: String): String? {
    return keys.firstNotNullOfOrNull { key ->
        this.entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value
            ?.takeIf { it.isNotBlank() }
    }
}

internal fun Map<String, String>.readCategoryName(): String? {
    return getFirstNonBlank("Category", "category")
        ?.trim()
        ?.takeIf { it.isNotBlank() }
}

internal fun Map<String, String>.matchesCategory(selectedCategory: String): Boolean {
    return readCategoryName()?.equals(selectedCategory, ignoreCase = true) == true
}

internal fun Map<String, String>.basketKey(): String {
    return getFirstNonBlank("PartId", "partid", "id", "PartNumber", "sku", "Name", "name")
        ?: "unknown-part"
}

internal fun Map<String, String>.readStockQuantity(): Int {
    return getFirstNonBlank("StockQuantity", "stockquantity", "stock", "quantity", "qty", "inventory")
        ?.toIntOrNull()
        ?.coerceAtLeast(0)
        ?: 0
}

internal fun String?.toPriceLabel(): String {
    val number = this?.toDoubleOrNull()
    return if (number == null) "-" else "€%.2f".format(number)
}

internal fun Int.toStockLabel(): String {
    return when {
        this <= 0 -> "Out of stock"
        this <= 5 -> "Low stock ($this)"
        else -> "In stock ($this)"
    }
}
