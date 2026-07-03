package com.nfctags.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FieldNameRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getFieldLabels(): List<String> {
        val saved = prefs.getString(KEY_LABELS, null)
        if (saved != null) {
            val parts = saved.split(FIELD_SEPARATOR, limit = 10)
            if (parts.size == 10) return parts
        }
        return DEFAULT_LABELS
    }

    fun getFieldLabel(index: Int): String {
        val labels = getFieldLabels()
        return if (index in labels.indices) labels[index] else "Valor ${index + 1}"
    }

    fun setFieldLabels(labels: List<String>) {
        val normalized = if (labels.size >= 10) labels.take(10)
            else labels + DEFAULT_LABELS.drop(labels.size)
        prefs.edit().putString(KEY_LABELS, normalized.joinToString(FIELD_SEPARATOR)).apply()
    }

    fun resetToDefaults() {
        prefs.edit().remove(KEY_LABELS).apply()
    }

    companion object {
        private const val PREFS_NAME = "acuigen_field_labels"
        private const val KEY_LABELS = "field_labels"
        private const val FIELD_SEPARATOR = "||"

        val DEFAULT_LABELS = listOf(
            "Valor 1", "Valor 2", "Valor 3", "Valor 4", "Valor 5",
            "Valor 6", "Valor 7", "Valor 8", "Valor 9", "Valor 10"
        )
    }
}
