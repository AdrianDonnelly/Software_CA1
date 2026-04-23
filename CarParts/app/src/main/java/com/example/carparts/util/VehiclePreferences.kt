package com.example.carparts.util

import android.content.Context

data class SelectedVehicle(
    val id: String,
    val make: String,
    val model: String,
    val year: String,
    val engineType: String = ""
) {
    val displayName: String get() = buildString {
        if (year.isNotBlank()) append("$year ")
        append(make)
        if (model.isNotBlank()) append(" $model")
    }
}

object VehiclePreferences {
    private const val PREFS_NAME = "vehicle_prefs"
    private const val KEY_ID = "vehicle_id"
    private const val KEY_MAKE = "vehicle_make"
    private const val KEY_MODEL = "vehicle_model"
    private const val KEY_YEAR = "vehicle_year"
    private const val KEY_ENGINE = "vehicle_engine"

    fun save(context: Context, vehicle: SelectedVehicle) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_ID, vehicle.id)
            .putString(KEY_MAKE, vehicle.make)
            .putString(KEY_MODEL, vehicle.model)
            .putString(KEY_YEAR, vehicle.year)
            .putString(KEY_ENGINE, vehicle.engineType)
            .apply()
    }

    fun load(context: Context): SelectedVehicle? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_ID, null) ?: return null
        return SelectedVehicle(
            id = id,
            make = prefs.getString(KEY_MAKE, "") ?: "",
            model = prefs.getString(KEY_MODEL, "") ?: "",
            year = prefs.getString(KEY_YEAR, "") ?: "",
            engineType = prefs.getString(KEY_ENGINE, "") ?: ""
        )
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
