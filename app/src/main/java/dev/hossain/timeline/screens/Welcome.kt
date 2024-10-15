package dev.hossain.timeline.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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

@Parcelize
data object WelcomeScreen : Screen {
    data class State(
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object NextClicked : Event()
    }
}

class WelcomePresenter
    @AssistedInject
    constructor(
        @Assisted private val navigator: Navigator,
    ) : Presenter<WelcomeScreen.State> {
        @Composable
        override fun present(): WelcomeScreen.State {
            // Or a flow!
            // val emails by emailRepository.getEmailsFlow().collectAsState(initial = emptyList())
            return WelcomeScreen.State { event ->
                when (event) {
                    // Navigate to the detail screen when an email is clicked
                    is WelcomeScreen.Event.NextClicked -> navigator.goTo(TimelineDataScreen)
                }
            }
        }

        @CircuitInject(WelcomeScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(navigator: Navigator): WelcomePresenter
        }
    }

@CircuitInject(screen = WelcomeScreen::class, scope = AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreenMain(
    state: WelcomeScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Location Heatmap",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
        },
        modifier = Modifier.padding(24.dp),
    ) { innerPadding ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Visualize Your Location History",
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Import your location data and explore your travel patterns with an interactive heatmap.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { state.eventSink(WelcomeScreen.Event.NextClicked) }) {
                Text("Get Started")
            }
        }
    }
}
