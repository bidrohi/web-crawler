import io.ktor.http.Url

data class Entry(
    val uri: Url,
    val depth: Int,
)
