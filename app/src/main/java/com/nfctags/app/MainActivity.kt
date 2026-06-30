package com.nfctags.app

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nfctags.app.ui.screens.HistoryScreen
import com.nfctags.app.ui.screens.HomeScreen
import com.nfctags.app.ui.screens.TagDetailScreen
import com.nfctags.app.ui.theme.NfcTagsTheme
import com.nfctags.app.viewmodel.TagViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var pendingTag: Tag? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            NfcTagsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: TagViewModel = hiltViewModel()
                    val navController = rememberNavController()
                    var nfcEscaneando by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        viewModel.nfcTagCapturado.collect { tag ->
                            viewModel.procesarTagNfc(tag)
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onTagClick = { tagId ->
                                    viewModel.seleccionarTag(tagId)
                                    navController.navigate("detail")
                                },
                                onNuevoTag = {
                                    viewModel.crearNuevoTag()
                                    navController.navigate("detail")
                                },
                                onScanNfc = {
                                    nfcEscaneando = true
                                }
                            )
                        }

                        composable("detail") {
                            TagDetailScreen(
                                viewModel = viewModel,
                                onBack = {
                                    navController.popBackStack()
                                },
                                onHistory = { tagId ->
                                    navController.navigate("history/$tagId")
                                }
                            )
                        }

                        composable("history/{tagId}") { backStackEntry ->
                            val tagId = backStackEntry.arguments?.getString("tagId") ?: return@composable
                            HistoryScreen(
                                viewModel = viewModel,
                                tagId = tagId,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == intent.action
        ) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                pendingTag = tag
                // La notificación se maneja desde el ViewModel a través del flujo
            }
        }
    }

    fun getPendingTag(): Tag? = pendingTag
    fun clearPendingTag() { pendingTag = null }
}
