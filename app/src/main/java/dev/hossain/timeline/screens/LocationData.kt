package dev.hossain.timeline.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
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
                        scope.launch {
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
                Timber.d("Parsed timeline data: $timelineData")
                Toast
                    .makeText(
                        context,
                        "Parsed timeline data successfully. Got ${timelineData.rawSignals.size} raw signals and ${timelineData.semanticSegments} semantic segments.",
                        Toast.LENGTH_SHORT,
                    ).show()

                val latLngList: List<LatLng> = timelineData.rawSignals.mapNotNull {
                    it.position?.latLng?.let { latLngString ->
                        val latLng = latLngString.split(", ")
                        LatLng(
                            /* latitude = */ latLng[0].removeSuffix("°").toDouble(),
                            /* longitude = */ latLng[1].removeSuffix("°").toDouble()
                        )
                    }
                }

                return latLngList.mapIndexed { index, latLng ->
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




@Composable
fun GoogleMapClustering(items: List<TimelineClusterItem>) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(northAmerica, 6f)
        }
    ) {
        DefaultClustering(
            items = items,
        )

        MarkerInfoWindow(
            state = rememberMarkerState(position = northAmerica),
            onClick = {
                Timber.d( "Non-cluster marker clicked! $it")
                true
            }
        )
    }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
private fun DefaultClustering(items: List<TimelineClusterItem>) {
    Clustering(
        items = items,
        // Optional: Handle clicks on clusters, cluster items, and cluster item info windows
        onClusterClick = {
            Timber.d( "Cluster clicked! $it")
            false
        },
        onClusterItemClick = {
            Timber.d( "Cluster item clicked! $it")
            false
        },
        onClusterItemInfoWindowClick = {
            Timber.d( "Cluster item info window clicked! $it")
        },
        // Optional: Custom rendering for non-clustered items
        clusterItemContent = null
    )
}

data class TimelineClusterItem(
    val itemPosition: LatLng,
    val itemTitle: String,
    val itemSnippet: String,
    val itemZIndex: Float,
) : ClusterItem {
    override fun getPosition(): LatLng =
        itemPosition

    override fun getTitle(): String =
        itemTitle

    override fun getSnippet(): String =
        itemSnippet

    override fun getZIndex(): Float =
        itemZIndex
}