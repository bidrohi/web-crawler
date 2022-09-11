import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.jsoup.Jsoup
import java.io.InputStream

abstract class Droid(
    private val debugLogEnabled: Boolean = false
) {
    abstract val name: String

    abstract fun invoke(uri: String, depth: Int): List<String>

    private val client by lazy { HttpClient(CIO) }

    protected fun debugLog(msg: String) {
        if (debugLogEnabled) println(msg)
    }

    protected suspend fun crawlAndQueue(entry: Entry, addEntry: (Entry) -> Unit) {
        debugLog("Crawling ${entry.uri}...")
        val httpResponse = client.get(entry.uri)
        if (httpResponse.status.value in 200..299) {
            val doc = Jsoup.parse(httpResponse.body<InputStream>(), Charsets.UTF_8.name(), entry.uri.toString())
            val anchors = doc.getElementsByTag("a")
            for (anchor in anchors) {
                val newUri = anchor.absUrl("href")
                try {
                    addEntry(Entry(Url(newUri), entry.depth - 1))
                } catch (e: URLDecodeException) {
                    // debugLog("Error: ($newUri) $e")
                } catch (e: URLParserException) {
                    // debugLog("Error: ($newUri) $e")
                }
            }
        }
    }
}
