package dev.hossain.timeline

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Parcelize
data object InboxScreen : Screen {
    data class State(
        val emails: List<Email>
    ) : CircuitUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inbox(state: InboxScreen.State, modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier, topBar = { TopAppBar(title = { Text("Inbox") }) }) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(state.emails) { email ->
                EmailItem(email)
            }
        }
    }
}

// Write one or use EmailItem from ui.kt
/** A simple email item to show in a list. */
@Composable
fun EmailItem(email: Email, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Row(
        modifier.clickable(onClick = onClick).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            Icons.Default.Person,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Magenta).padding(4.dp),
            colorFilter = ColorFilter.tint(Color.White),
            contentDescription = null,
        )
        Column {
            Row {
                Text(
                    text = email.sender,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = email.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.5f),
                )
            }

            Text(text = email.subject, style = MaterialTheme.typography.labelLarge)
            Text(
                text = email.body,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(0.5f),
            )
        }
    }
}

data class Email(
    val id: String,
    val subject: String,
    val body: String,
    val sender: String,
    val timestamp: String,
    val recipients: List<String>
)


class InboxPresenter(private val emailRepository: EmailRepository) : Presenter<InboxScreen.State> {
    @Composable
    override fun present(): InboxScreen.State {
        val emails by produceState<List<Email>>(initialValue = emptyList()) {
            value = emailRepository.getEmails()
        }
        // Or a flow!
        // val emails by emailRepository.getEmailsFlow().collectAsState(initial = emptyList())
        return InboxScreen.State(emails)
    }
}

class EmailRepository {
    fun getEmails(): List<Email> {
        return listOf(
            Email(
                id = "1",
                subject = "Meeting re-sched!",
                body = "Hey, I'm going to be out of the office tomorrow. Can we reschedule?",
                sender = "Ali Connors",
                timestamp = "3:00 PM",
                recipients = listOf("a@example.com"),
            )
        )
    }
}


