package com.nfctags.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SensorDoor
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nfctags.app.auth.AuthViewModel
import com.nfctags.app.ui.components.TagCard
import com.nfctags.app.util.CsvExporter
import com.nfctags.app.viewmodel.TagEvent
import com.nfctags.app.viewmodel.TagViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TagViewModel,
    authViewModel: AuthViewModel,
    onTagClick: (String) -> Unit,
    onNuevoTag: () -> Unit,
    onScanNfc: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val tags by viewModel.allTags.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFieldSettings by remember { mutableStateOf(false) }
    val currentLabels = uiState.fieldLabels.ifEmpty {
        listOf("Valor 1","Valor 2","Valor 3","Valor 4","Valor 5","Valor 6","Valor 7","Valor 8","Valor 9","Valor 10")
    }
    var fieldLabelsEdit by remember { mutableStateOf(currentLabels) }

    LaunchedEffect(uiState.fieldLabels) {
        if (uiState.fieldLabels.isNotEmpty()) {
            fieldLabelsEdit = uiState.fieldLabels
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

    if (showFieldSettings) {
        AlertDialog(
            onDismissRequest = { showFieldSettings = false },
            title = { Text("Nombres de campo") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Personaliza los nombres de los 10 campos para cada tag.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Divider()
                    fieldLabelsEdit.forEachIndexed { index, label ->
                        OutlinedTextField(
                            value = label,
                            onValueChange = { newVal ->
                                fieldLabelsEdit = fieldLabelsEdit.toMutableList().apply {
                                    set(index, newVal)
                                }
                            },
                            label = { Text("Campo ${index + 1}") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.actualizarNombresCampos(fieldLabelsEdit)
                    showFieldSettings = false
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.resetearNombresCampos()
                        showFieldSettings = false
                    }) {
                        Text("Restablecer")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showFieldSettings = false }) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AcuiGen",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tags.size} etiquetas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFieldSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurar campos"
                        )
                    }
                    IconButton(onClick = { viewModel.forzarSincronizacion() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sincronizar"
                        )
                    }
                    IconButton(onClick = {
                        CsvExporter.exportAllTags(context, tags)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Exportar CSV"
                        )
                    }
                    IconButton(onClick = onScanNfc) {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = "Escanear NFC"
                        )
                    }
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuevoTag,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuevo tag"
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedVisibility(
                visible = uiState.scanning,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                com.nfctags.app.ui.components.NfcScanOverlay()
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (tags.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SensorDoor,
                        contentDescription = null,
                        modifier = Modifier.height(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No hay etiquetas NFC",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Escanea un tag NFC o crea uno manualmente",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(onClick = onScanNfc) {
                        Icon(Icons.Default.Nfc, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Escanear NFC")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tags, key = { it.id }) { tag ->
                        TagCard(
                            tag = tag,
                            onClick = { onTagClick(tag.id) }
                        )
                    }
                }
            }
        }
    }
}
