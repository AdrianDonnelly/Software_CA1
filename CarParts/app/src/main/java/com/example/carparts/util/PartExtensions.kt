package com.example.carparts.util

internal fun Map<String, String>.getFirstNonBlank(vararg keys: String): String? {
    return keys.firstNotNullOfOrNull { key ->
        this.entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value
            ?.takeIf { it.isNotBlank() }
    }
}

internal fun Map<String, String>.readCategoryName(): String? {
    return getFirstNonBlank("Category")?.trim()
}

internal fun Map<String, String>.matchesCategory(selectedCategory: String): Boolean {
    return readCategoryName()?.equals(selectedCategory, ignoreCase = true) == true
}

internal fun Map<String, String>.basketKey(): String {
    return getFirstNonBlank("PartId") ?: "unknown-part"
}

internal fun Map<String, String>.readStockQuantity(): Int {
    return getFirstNonBlank("StockQuantity")
        ?.toIntOrNull()
        ?.coerceAtLeast(0)
        ?: 0
}

internal fun String?.toPriceLabel(): String {
    val number = this?.toDoubleOrNull()
    return if (number == null) "-" else "€%.2f".format(number)
}
