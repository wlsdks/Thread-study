package com.study.thread.study.coroutine.suspend


import kotlinx.coroutines.*

fun main() = runBlocking {
    launch {
        println("추가 코루틴 시작 - 스레드: ${Thread.currentThread().name}")
        repeat(5) { i ->
            println("추가 코루틴 실행 중... $i - 스레드: ${Thread.currentThread().name}")
            delay(500)
        }
    }
    processData()
}

suspend fun processData() {
    val data = fetchData()
    val processedData = withContext(Dispatchers.Default) {
        transformData(data) // 별도의 스레드에서 실행
    }
    println("결과 출력 - 스레드: ${Thread.currentThread().name}")
}

suspend fun fetchData(): String {
    println("fetchData 시작 - 스레드: ${Thread.currentThread().name}")
    delay(1000)
    println("fetchData 완료 - 스레드: ${Thread.currentThread().name}")
    return "Data fetched"
}

fun transformData(input: String): String {
    println("transformData 시작 - 스레드: ${Thread.currentThread().name}")
    Thread.sleep(1000) // 스레드 차단 작업
    println("transformData 완료 - 스레드: ${Thread.currentThread().name}")
    return input.uppercase()
}
