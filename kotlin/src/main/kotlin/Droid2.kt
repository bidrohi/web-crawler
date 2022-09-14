
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import java.util.concurrent.TimeUnit

class Droid2(
    debugLogEnabled: Boolean = false
) : Droid(debugLogEnabled) {
    override val name = "Droid2"

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun invoke(uri: String, depth: Int): List<String> {
        debugLog("Starting to crawl...")
        val channel = Channel<Entry>(50)
        channel.trySend(Entry(Url(uri), depth))
        val addScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val uris = mutableSetOf<String>()
        val visited = mutableSetOf<String>()
        repeat(10) {
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                channel.consumeEach { entry ->
                    uris.add(entry.uri.toString())
                    if (entry.depth > 0 && !visited.contains(entry.uri.toString())) {
                        visited.add(entry.uri.toString())
                        crawlAndQueue(entry) {
                            addScope.launch { channel.send(it) }
                        }
                    }
                }
            }
        }
        runBlocking {
            // TODO: Need better end detection
            do {
                delay(TimeUnit.SECONDS.toMillis(10))
            } while (!channel.isEmpty)
            channel.close()
        }
        return uris.toList()
    }
}
