import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

suspend fun fetchData(): String {
    delay(1000)
    return "Data fetched"
}

fun transformData(input: String): String {
    Thread.sleep(1000)
    return input.uppercase()
}

suspend fun processData() {
    val time = measureTimeMillis {
        val data = fetchData()
        val processedData = withContext(Dispatchers.Default) {
            transformData(data)
        }
        println(processedData)
    }
    println("Processing completed in $time ms")
}

fun main() = runBlocking {
    processData()
}

