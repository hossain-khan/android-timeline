package dev.hossain.timeline

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
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import dev.hossain.timeline.ui.theme.TimelineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val emailRepository = EmailRepository()
        val circuit: Circuit =
            Circuit.Builder()
                .addPresenter<InboxScreen, InboxScreen.State>(InboxPresenter(emailRepository))
                .addUi<InboxScreen, InboxScreen.State> { state, modifier -> Inbox(state, modifier) }
                .build()

        setContent {
            TimelineTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    CircuitCompositionLocals(circuit) {
                        CircuitContent(InboxScreen, Modifier.padding(padding))
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TimelineTheme {
        Greeting("Android")
    }
}