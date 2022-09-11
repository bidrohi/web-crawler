import io.ktor.http.*
import kotlinx.coroutines.runBlocking

class Droid1(
    debugLogEnabled: Boolean = false
) : Droid(debugLogEnabled) {
    override val name = "Droid1"

    override fun invoke(uri: String, depth: Int): List<String> {
        debugLog("Starting to crawl...")
        val queue = mutableListOf<Entry>()
        queue.add(Entry(Url(uri), depth))
        val uris = mutableSetOf<String>()
        val visited = mutableSetOf<String>()
        while (queue.isNotEmpty()) {
            val entry = queue.removeAt(0)
            uris.add(entry.uri.toString())
            if (entry.depth > 0 && !visited.contains(entry.uri.toString())) {
                visited.add(entry.uri.toString())
                runBlocking { crawlAndQueue(entry, queue::add) }
            }
        }
        return uris.toList()
    }
}
