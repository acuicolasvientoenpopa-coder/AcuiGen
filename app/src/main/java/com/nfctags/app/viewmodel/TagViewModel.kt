package com.nfctags.app.viewmodel

import android.app.Application
import android.nfc.Tag
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nfctags.app.data.entities.TagEntity
import com.nfctags.app.data.entities.ValueHistoryEntity
import com.nfctags.app.data.repository.TagRepository
import com.nfctags.app.nfc.NfcHandler
import com.nfctags.app.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagUiState(
    val tags: List<TagEntity> = emptyList(),
    val selectedTag: TagEntity? = null,
    val history: List<ValueHistoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val scanning: Boolean = false,
    val error: String? = null,
    val syncEnabled: Boolean = true
)

sealed class TagEvent {
    data class Success(val mensaje: String) : TagEvent()
    data class Error(val mensaje: String) : TagEvent()
    data class NfcScanned(val tagId: String) : TagEvent()
    data class TagSaved(val tag: TagEntity) : TagEvent()
    data class TagDeleted(val tagId: String) : TagEvent()
}

@HiltViewModel
class TagViewModel @Inject constructor(
    application: Application,
    private val repository: TagRepository,
    private val nfcHandler: NfcHandler,
    private val syncManager: SyncManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TagUiState())
    val uiState: StateFlow<TagUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TagEvent>()
    val events = _events.asSharedFlow()

    val allTags = repository.observeAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _nfcTagCapturado = MutableSharedFlow<Tag>()
    val nfcTagCapturado = _nfcTagCapturado.asSharedFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.observeAllTags().collect { tags ->
                    _uiState.value = _uiState.value.copy(
                        tags = tags,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar datos: ${e.message}"
                )
            }
        }
    }

    fun procesarTagNfc(tag: Tag) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(scanning = true)
            try {
                val resultado = nfcHandler.leerTag(tag)
                if (resultado != null) {
                    val existente = repository.getTag(resultado.tagId)
                    if (existente != null) {
                        _uiState.value = _uiState.value.copy(
                            selectedTag = existente,
                            scanning = false
                        )
                        _events.emit(TagEvent.NfcScanned(resultado.tagId))
                        _events.emit(TagEvent.Success("Tag existente: ${existente.nombre}"))
                    } else {
                        val nuevoTag = TagEntity(
                            id = resultado.tagId,
                            nombre = "Tag ${resultado.tagId.takeLast(8)}",
                            valor1 = "", valor2 = "", valor3 = "",
                            valor4 = "", valor5 = "", valor6 = "",
                            valor7 = "", valor8 = "", valor9 = "", valor10 = ""
                        )
                        repository.saveTag(nuevoTag)
                        _uiState.value = _uiState.value.copy(
                            selectedTag = nuevoTag,
                            scanning = false
                        )
                        _events.emit(TagEvent.NfcScanned(resultado.tagId))
                        _events.emit(TagEvent.Success("Nuevo tag creado: ${nuevoTag.nombre}"))
                    }
                } else {
                    _uiState.value = _uiState.value.copy(scanning = false)
                    _events.emit(TagEvent.Error("No se pudo leer el tag NFC"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(scanning = false)
                _events.emit(TagEvent.Error("Error al leer NFC: ${e.message}"))
            }
        }
    }

    fun seleccionarTag(tagId: String) {
        viewModelScope.launch {
            try {
                val tag = repository.getTag(tagId)
                _uiState.value = _uiState.value.copy(selectedTag = tag)

                if (tag != null) {
                    repository.observeHistory(tagId).collect { history ->
                        _uiState.value = _uiState.value.copy(history = history)
                    }
                }
            } catch (e: Exception) {
                _events.emit(TagEvent.Error("Error al cargar tag: ${e.message}"))
            }
        }
    }

    fun guardarValoresTag(tagId: String, nombre: String, valores: Map<Int, String>) {
        viewModelScope.launch {
            try {
                repository.updateTagValues(tagId, nombre, valores)
                _events.emit(TagEvent.Success("Valores guardados correctamente"))

                if (syncManager.hayConexion()) {
                    syncManager.programarSync()
                }
            } catch (e: Exception) {
                _events.emit(TagEvent.Error("Error al guardar: ${e.message}"))
            }
        }
    }

    fun crearNuevoTag() {
        viewModelScope.launch {
            try {
                val tagId = nfcHandler.generarIdUnico()
                val nuevoTag = TagEntity(
                    id = tagId,
                    nombre = "Nuevo Tag",
                    valor1 = "", valor2 = "", valor3 = "",
                    valor4 = "", valor5 = "", valor6 = "",
                    valor7 = "", valor8 = "", valor9 = "", valor10 = ""
                )
                repository.saveTag(nuevoTag)
                _uiState.value = _uiState.value.copy(selectedTag = nuevoTag)
                _events.emit(TagEvent.TagSaved(nuevoTag))
                _events.emit(TagEvent.Success("Nuevo tag creado manualmente"))
            } catch (e: Exception) {
                _events.emit(TagEvent.Error("Error al crear tag: ${e.message}"))
            }
        }
    }

    fun eliminarTag(tagId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTag(tagId)
                _uiState.value = _uiState.value.copy(selectedTag = null)
                _events.emit(TagEvent.TagDeleted(tagId))
                _events.emit(TagEvent.Success("Tag eliminado"))

                if (syncManager.hayConexion()) {
                    syncManager.programarSync()
                }
            } catch (e: Exception) {
                _events.emit(TagEvent.Error("Error al eliminar: ${e.message}"))
            }
        }
    }

    fun forzarSincronizacion() {
        viewModelScope.launch {
            try {
                if (syncManager.hayConexion()) {
                    syncManager.programarSync()
                    _events.emit(TagEvent.Success("Sincronización iniciada"))
                } else {
                    _events.emit(TagEvent.Error("Sin conexión a internet"))
                }
            } catch (e: Exception) {
                _events.emit(TagEvent.Error("Error: ${e.message}"))
            }
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun limpiarSeleccion() {
        _uiState.value = _uiState.value.copy(selectedTag = null)
    }
}
