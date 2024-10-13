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
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import dev.hossain.timeline.ui.theme.TimelineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val emailRepository = EmailRepository()
        val circuit: Circuit =
            Circuit.Builder()
                // TODO Update circuit tutorial code here
                //.addPresenter<InboxScreen, InboxScreen.State>(InboxPresenter(emailRepository))
                .addPresenterFactory(InboxPresenter.Factory(emailRepository))
                .addUi<InboxScreen, InboxScreen.State> { state, modifier -> Inbox(state, modifier) }
                .addPresenterFactory(DetailPresenter.Factory(emailRepository))
                // TODO Update circuit tutorial code here first param should be the state
                .addUi<DetailScreen, DetailScreen.State> { state, modifier -> EmailDetailContent(state, modifier) }
                .build()



        setContent {
            TimelineTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    val backStack = rememberSaveableBackStack(root = InboxScreen)
                    val navigator = rememberCircuitNavigator(backStack) {
                        // Do something when the root screen is popped, usually exiting the app
                    }
                    CircuitCompositionLocals(circuit) {
                        NavigableCircuitContent(navigator = navigator, backStack = backStack,  Modifier.padding(padding))
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