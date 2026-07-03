package com.nfctags.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.nfctags.app.util.CsvExporter
import com.nfctags.app.viewmodel.TagEvent
import com.nfctags.app.viewmodel.TagViewModel

val nombresCamposDefault = listOf(
    "Valor 1", "Valor 2", "Valor 3", "Valor 4", "Valor 5",
    "Valor 6", "Valor 7", "Valor 8", "Valor 9", "Valor 10"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagDetailScreen(
    viewModel: TagViewModel,
    onBack: () -> Unit,
    onHistory: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val tag = uiState.selectedTag
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var nombre by remember(tag) { mutableStateOf(tag?.nombre ?: "") }
    var valores by remember(tag) {
        mutableStateOf(
            tag?.let { t ->
                listOf(
                    t.valor1, t.valor2, t.valor3, t.valor4, t.valor5,
                    t.valor6, t.valor7, t.valor8, t.valor9, t.valor10
                )
            } ?: List(10) { "" }
        )
    }

    var edited by remember { mutableStateOf(false) }

    LaunchedEffect(tag) {
        if (tag != null) {
            nombre = tag.nombre
            valores = listOf(
                tag.valor1, tag.valor2, tag.valor3, tag.valor4, tag.valor5,
                tag.valor6, tag.valor7, tag.valor8, tag.valor9, tag.valor10
            )
            edited = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TagEvent.Success -> snackbarHostState.showSnackbar(event.mensaje)
                is TagEvent.Error -> snackbarHostState.showSnackbar(event.mensaje)
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tag?.nombre ?: "Detalle",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { tag?.let { onHistory(it.id) } }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Historial"
                        )
                    }
                    IconButton(onClick = {
                        if (tag != null) {
                            val labels = uiState.fieldLabels.ifEmpty { nombresCamposDefault }
                            CsvExporter.exportTagWithHistory(
                                context, tag, uiState.history, labels
                            )
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Exportar CSV"
                        )
                    }
                    IconButton(onClick = {
                        tag?.let { viewModel.eliminarTag(it.id) }
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (tag == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Selecciona o escanea un tag",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ID del Tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = tag.id,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    edited = true
                },
                label = { Text("Nombre de la etiqueta") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Valores personalizados (10 campos)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            valores.forEachIndexed { index, valor ->
                OutlinedTextField(
                    value = valor,
                    onValueChange = { nuevo ->
                        valores = valores.toMutableList().apply { set(index, nuevo) }
                        edited = true
                    },
                    label = {
                        val labels = uiState.fieldLabels.ifEmpty { nombresCamposDefault }
                        Text(labels.getOrElse(index) { "Valor ${index + 1}" })
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = if (index < 9) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val mapaValores = valores.mapIndexed { idx, v -> idx + 1 to v }.toMap()
                        viewModel.guardarValoresTag(tag.id, nombre, mapaValores)
                        edited = false
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = edited,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar")
                }

                OutlinedButton(
                    onClick = { onHistory(tag.id) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Historial")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
