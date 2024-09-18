package com.study.thread.study.coroutine.suspend

import kotlinx.coroutines.*

fun main() = runBlocking {
    val job = launch {
        println("추가 코루틴 시작 - 스레드: ${Thread.currentThread().name}")
        repeat(5) { i ->
            println("추가 코루틴 실행 중... $i - 스레드: ${Thread.currentThread().name}")
            delay(500)
        }
    }
    job.join() // 추가 코루틴이 완료될 때까지 기다림
    joinProcessData()
}

suspend fun joinProcessData() {
    val data = joinFetchData()
    val joinProcessedData = joinTransformData(data)
    println("결과 출력 - 스레드: ${Thread.currentThread().name}")
}

suspend fun joinFetchData(): String {
    println("fetchData 시작 - 스레드: ${Thread.currentThread().name}")
    delay(1000)
    println("fetchData 완료 - 스레드: ${Thread.currentThread().name}")
    return "Data fetched"
}

fun joinTransformData(input: String): String {
    println("transformData 시작 - 스레드: ${Thread.currentThread().name}")
    Thread.sleep(1000) // 스레드 차단 작업
    println("transformData 완료 - 스레드: ${Thread.currentThread().name}")
    return input.uppercase()
}
