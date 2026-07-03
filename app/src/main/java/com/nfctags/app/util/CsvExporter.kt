package com.nfctags.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.nfctags.app.data.entities.TagEntity
import com.nfctags.app.data.entities.ValueHistoryEntity
import java.io.File

object CsvExporter {

    fun exportAllTags(context: Context, tags: List<TagEntity>) {
        val csv = buildString {
            appendLine("ID,Nombre,Valor1,Valor2,Valor3,Valor4,Valor5,Valor6,Valor7,Valor8,Valor9,Valor10,Creado,Actualizado")
            tags.forEach { t ->
                appendLine("${t.id},${csvEscape(t.nombre)},${csvEscape(t.valor1)},${csvEscape(t.valor2)},${csvEscape(t.valor3)},${csvEscape(t.valor4)},${csvEscape(t.valor5)},${csvEscape(t.valor6)},${csvEscape(t.valor7)},${csvEscape(t.valor8)},${csvEscape(t.valor9)},${csvEscape(t.valor10)},${t.createdAt},${t.updatedAt}")
            }
        }
        shareCsv(context, csv, "tags_export.csv")
    }

    fun exportTagWithHistory(
        context: Context,
        tag: TagEntity,
        history: List<ValueHistoryEntity>,
        fieldLabels: List<String>
    ) {
        val csv = buildString {
            appendLine("=== TAG ===")
            appendLine("ID,Nombre,${fieldLabels.joinToString(",")},Creado,Actualizado")
            appendLine("${tag.id},${csvEscape(tag.nombre)},${tag.valor1},${tag.valor2},${tag.valor3},${tag.valor4},${tag.valor5},${tag.valor6},${tag.valor7},${tag.valor8},${tag.valor9},${tag.valor10},${tag.createdAt},${tag.updatedAt}")
            appendLine()
            appendLine("=== HISTORIAL ===")
            appendLine("Campo,Valor Anterior,Valor Nuevo,Fecha")
            history.forEach { h ->
                appendLine("${h.campo},${csvEscape(h.valorAnterior)},${csvEscape(h.valorNuevo)},${h.timestamp}")
            }
        }
        shareCsv(context, csv, "tag_${tag.id.takeLast(8)}_export.csv")
    }

    private fun shareCsv(context: Context, content: String, filename: String) {
        val file = File(context.cacheDir, filename)
        file.writeText(content, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Exportar $filename"))
    }

    private fun csvEscape(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
