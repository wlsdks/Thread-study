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
    dispatcherProcessData()
}

suspend fun dispatcherProcessData() {
    val data = dispatcherFetchData()
    val processedData = withContext(Dispatchers.Default) {
        dispatcherTransformData(data) // 별도의 스레드에서 실행
    }
    println("결과 출력 - 스레드: ${Thread.currentThread().name}")
}

suspend fun dispatcherFetchData(): String {
    println("fetchData 시작 - 스레드: ${Thread.currentThread().name}")
    delay(1000)
    println("fetchData 완료 - 스레드: ${Thread.currentThread().name}")
    return "Data fetched"
}

fun dispatcherTransformData(input: String): String {
    println("transformData 시작 - 스레드: ${Thread.currentThread().name}")
    Thread.sleep(1000) // 스레드 차단 작업
    println("transformData 완료 - 스레드: ${Thread.currentThread().name}")
    return input.uppercase()
}
