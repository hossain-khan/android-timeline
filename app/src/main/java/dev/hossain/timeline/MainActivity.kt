package dev.hossain.timeline

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.squareup.anvil.annotations.ContributesMultibinding
import dev.hossain.timeline.di.ActivityKey
import dev.hossain.timeline.di.AppScope
import dev.hossain.timeline.ui.theme.TimelineTheme
import javax.inject.Inject

@ContributesMultibinding(AppScope::class, boundType = Activity::class)
@ActivityKey(MainActivity::class)
class MainActivity @Inject constructor(private val circuit: Circuit) : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            TimelineTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    val backStack = rememberSaveableBackStack(root = InboxScreen)
                    val navigator = rememberCircuitNavigator(backStack) {
                        // Do something when the root screen is popped, usually exiting the app
                    }
                    CircuitCompositionLocals(circuit) {
                        NavigableCircuitContent(
                            navigator = navigator,
                            backStack = backStack,
                            Modifier.padding(padding)
                        )
                    }
                }
            }
        }
    }
}