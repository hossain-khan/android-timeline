package dev.hossain.timeline.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dev.hossain.timeline.Parser
import dev.hossain.timeline.di.AppScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.io.InputStream

val northAmerica = LatLng(43.2606921, -80.0979546)

@Parcelize
data object TimelineDataScreen : Screen {
    data class State(
        val items: List<TimelineClusterItem>,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class FileSelected(
            val fileUri: Uri,
        ) : Event()
    }
}

class TimelineDataPresenter
    @AssistedInject
    constructor(
        @Assisted private val navigator: Navigator,
    ) : Presenter<TimelineDataScreen.State> {
        @Composable
        override fun present(): TimelineDataScreen.State {
            var items = remember { mutableStateOf(emptyList<TimelineClusterItem>()) }
            val scope = rememberCoroutineScope()
            val context: Context = LocalContext.current

            return TimelineDataScreen.State(items.value) { event ->
                when (event) {
                    is TimelineDataScreen.Event.FileSelected -> {
                        Timber.i("User selected file: %s", event.fileUri)
                        scope.launch(Dispatchers.IO) {
                            val data = loadFileData(context, event.fileUri)
                            Timber.i("Loaded data: %s", data.size)
                            items.value = data
                        }
                    }
                }
            }
        }

        @CircuitInject(TimelineDataScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(navigator: Navigator): TimelineDataPresenter
        }

        private suspend fun loadFileData(
            context: Context,
            fileUri: Uri,
        ): List<TimelineClusterItem> {
            val parser = Parser()

            val contentResolver = context.contentResolver

            contentResolver.openInputStream(fileUri)?.use { inputStream: InputStream ->
                val timelineData = parser.parse(inputStream)
                Timber.d(
                    "Parsed timeline data. Raw signals: ${timelineData.rawSignals.size}, Semantic Segments: ${timelineData.semanticSegments.size}",
                )

                val latLngSignalList: List<LatLng> =
                    timelineData.rawSignals.mapNotNull {
                        it.position?.latLng?.toLatLng()
                    }

                val latLngVisitList: List<LatLng> =
                    timelineData.semanticSegments.mapNotNull {
                        // Only pick the visit location
                        it.visit
                            ?.topCandidate
                            ?.placeLocation
                            ?.latLng
                            ?.toLatLng()
                    }

                val latLngPathList: List<LatLng> =
                    timelineData.semanticSegments
                        .map {
                            it.timelinePath.map { timelinePoint -> timelinePoint.point.toLatLng() }
                        }.flatten()

                return listOf(latLngVisitList, latLngPathList, latLngSignalList)
                    .flatten()
                    .mapIndexed { index, latLng ->
                        TimelineClusterItem(
                            itemPosition = latLng,
                            itemTitle = "Item $index",
                            itemSnippet = "Snippet $index",
                            itemZIndex = 0f,
                        )
                    }
            } ?: Timber.e("Failed to open input stream for URI: $fileUri")

            return emptyList()
        }
    }

@CircuitInject(TimelineDataScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSelectionScreen(
    state: TimelineDataScreen.State,
    modifier: Modifier = Modifier,
) {
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedFileUri = result.data?.data

                state.eventSink(TimelineDataScreen.Event.FileSelected(selectedFileUri!!))
            }
        }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Select Location Data") },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                val intent =
                    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        type = "application/json"
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                launcher.launch(intent)
            }) {
                Text("Select JSON File")
            }

            Spacer(modifier = Modifier.height(16.dp))

            selectedFileUri?.let {
                Text("Selected file: ${it.path}")
            }

            GoogleMapClustering(state.items)
        }
    }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun GoogleMapClustering(items: List<TimelineClusterItem>) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState =
            rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(northAmerica, 6f)
            },
        googleMapOptionsFactory = {
            GoogleMapOptions().apply {
                mapId("c9fea86305b2b256")
                compassEnabled(true)
                zoomControlsEnabled(false)
            }
        },
    ) {
        // Clustering data is useful for debugging the points on the map.
        DefaultClustering(
            items = items,
        )

        MarkerInfoWindow(
            state = rememberMarkerState(position = northAmerica),
            onClick = {
                Timber.d("Non-cluster marker clicked! $it")
                true
            },
        )

        // This code belongs inside the GoogleMap content block
        MapEffect(key1 = items) { map ->
            val latLngs: List<LatLng> = items.map { it.itemPosition }

            if (latLngs.isEmpty()) {
                return@MapEffect
            }

            // Create a heat map tile provider, passing it the latlngs of the police stations.
            val provider =
                HeatmapTileProvider
                    .Builder()
                    .data(latLngs)
                    .build()

            // Add a tile overlay to the map, using the heat map tile provider.
            map.addTileOverlay(TileOverlayOptions().tileProvider(provider))
        }
    }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
private fun DefaultClustering(items: List<TimelineClusterItem>) {
    Clustering(
        items = items,
        // Optional: Handle clicks on clusters, cluster items, and cluster item info windows
        onClusterClick = {
            Timber.d("Cluster clicked! $it")
            false
        },
        onClusterItemClick = {
            Timber.d("Cluster item clicked! $it")
            false
        },
        onClusterItemInfoWindowClick = {
            Timber.d("Cluster item info window clicked! $it")
        },
        // Optional: Custom rendering for non-clustered items
        clusterItemContent = null,
    )
}

data class TimelineClusterItem(
    val itemPosition: LatLng,
    val itemTitle: String,
    val itemSnippet: String,
    val itemZIndex: Float,
) : ClusterItem {
    override fun getPosition(): LatLng = itemPosition

    override fun getTitle(): String = itemTitle

    override fun getSnippet(): String = itemSnippet

    override fun getZIndex(): Float = itemZIndex
}

private fun String.toLatLng(): LatLng {
    val latLng = this.split(", ")
    return LatLng(
        latLng[0].removeSuffix("°").toDouble(),
        latLng[1].removeSuffix("°").toDouble(),
    )
}
