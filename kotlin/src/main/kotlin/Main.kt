
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

fun main(args: Array<String>) {
    val parser = ArgParser("crawler")
    val depth by parser.option(
        ArgType.Int,
        shortName = "d",
        fullName = "depth",
        description = "Number of depth to follow"
    ).default(2)
    val uri by parser.option(
        ArgType.String,
        shortName = "u",
        fullName = "uri",
        description = "Base url to start crawling from"
    ).default("https://www.engadget.com")
    parser.parse(args)

    println("Crawl benchmarking...")
    println("Crawling $uri upto $depth levels deep...")

    val droids = listOf(
        Droid1(),
        Droid2(),
    )
    for (d in droids) {
        crawlAndTime(d, uri, depth)
    }
}

@OptIn(ExperimentalTime::class)
fun crawlAndTime(d: Droid, uri: String, depth: Int) {
    println("Invoking ${d.name}")
    val (urls, elapsedTime) = measureTimedValue { d.invoke(uri, depth) }
    println("=> found ${urls.size} distinct URLs")
    println("=> took $elapsedTime")
}
