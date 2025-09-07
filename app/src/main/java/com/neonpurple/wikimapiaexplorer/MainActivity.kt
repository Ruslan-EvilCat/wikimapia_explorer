package com.neonpurple.wikimapiaexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.neonpurple.wikimapiaexplorer.ui.theme.WikimapiaExplorerTheme
import com.neonpurple.wikimapiaexplorer.ui.MapViewModel
import com.neonpurple.wikimapiaexplorer.ui.PlaceUi
import com.neonpurple.wikimapiaexplorer.ui.MapScreen

class MainActivity : ComponentActivity() {
    private val viewModel: MapViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WikimapiaExplorerTheme {
                AppContent(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppContent(vm: MapViewModel) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val state by vm.uiState.collectAsState(initial = vm.uiState.value)
    var selectedPlace by remember { mutableStateOf<PlaceUi?>(null) }

    MapScreen(
        modifier = Modifier.fillMaxSize(),
        places = state.places,
        onPlaceSelected = { selectedPlace = it },
        onRecenterClick = { /* handled inside MapScreen */ },
        radius = state.radius,
        onChangeRadius = { vm.setRadius(it) }
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (selectedPlace != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedPlace = null },
            sheetState = sheetState
        ) {
            val p = selectedPlace!!
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                Text(text = p.title, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                Text(text = "${'$'}{p.lat}, ${'$'}{p.lon}", modifier = Modifier.padding(top = 8.dp))

                androidx.compose.material3.Button(
                    onClick = {
                        val clip = android.content.ClipboardManager::class.java
                        val cm = ctx.getSystemService(clip) as android.content.ClipboardManager
                        val clipData = android.content.ClipData.newPlainText("coords", "${'$'}{p.lat},${'$'}{p.lon}")
                        cm.setPrimaryClip(clipData)
                        android.widget.Toast.makeText(ctx, "Copied", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(top = 12.dp)
                ) { Text("Copy coords") }

                androidx.compose.material3.Button(
                    onClick = {
                        val uri = android.net.Uri.parse("geo:${'$'}{p.lat},${'$'}{p.lon}?q=${'$'}{p.lat},${'$'}{p.lon}(${p.title})")
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps")
                        runCatching { ctx.startActivity(intent) }
                            .onFailure {
                                // fallback to any maps app
                                ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
                            }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) { Text("Open in Google Maps") }

                androidx.compose.material3.Button(
                    onClick = {
                        val url = "https://wikimapia.org/#lat=${'$'}{p.lat}&lon=${'$'}{p.lon}&z=17&m=b"
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        ctx.startActivity(intent)
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) { Text("Open on Wikimapia") }

                Text(
                    text = "Data © Wikimapia (CC BY-SA)",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
