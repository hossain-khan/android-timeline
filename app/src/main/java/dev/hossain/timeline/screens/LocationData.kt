package dev.hossain.timeline.screens

import android.app.Activity
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dev.hossain.timeline.di.AppScope
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data object TimelineDataScreen : Screen {
    data class State(
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
        override fun present(): TimelineDataScreen.State =
            TimelineDataScreen.State { event ->
                when (event) {
                    is TimelineDataScreen.Event.FileSelected -> {
                        Timber.i("User selected file: %s", event.fileUri)
                    }
                }
            }

        @CircuitInject(TimelineDataScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(navigator: Navigator): TimelineDataPresenter
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
        }
    }
}
