package com.study.thread.study.coroutine.suspend

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    launch {
        println("추가 코루틴 시작 - 스레드: ${Thread.currentThread().name}")
        repeat(5) { i ->
            println("추가 코루틴 실행 중... $i - 스레드: ${Thread.currentThread().name}")
            delay(500)
        }
    }
    delayProcessData()
}

suspend fun delayProcessData() {
    val data = delayFetchData()
    val processedData = delayTransformData(data) // 메인 스레드 차단 없이 실행
    println("결과 출력 - 스레드: ${Thread.currentThread().name}")
}

suspend fun delayFetchData(): String {
    println("fetchData 시작 - 스레드: ${Thread.currentThread().name}")
    delay(1000)
    println("fetchData 완료 - 스레드: ${Thread.currentThread().name}")
    return "Data fetched"
}

suspend fun delayTransformData(input: String): String {
    println("transformData 시작 - 스레드: ${Thread.currentThread().name}")
    delay(1000) // 코루틴 일시 중단
    println("transformData 완료 - 스레드: ${Thread.currentThread().name}")
    return input.uppercase()
}
