package com.study.thread.study.coroutine

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors


fun main(): Unit = runBlocking {
    // 고정된 수의 스레드를 가진 스레드 풀 생성 (예: 2개 스레드)
    val threadPool = Executors.newFixedThreadPool(2)
    val customDispatcher = threadPool.asCoroutineDispatcher()

    // 첫 번째 코루틴: 커스텀 디스패처에서 실행
    launch(customDispatcher) {
        suspendFunction("Coroutine 1")
    }

    // 두 번째 코루틴: 커스텀 디스패처에서 실행
    launch(customDispatcher) {
        repeat(5) {
            delay(300)  // 0.3초마다 출력
            println("Other coroutine running: $it on ${Thread.currentThread().name}")
        }
    }

    // 메인 코루틴이 종료되기 전에 충분한 시간을 대기
    delay(3000)

    // 사용한 스레드 풀을 닫아 리소스 해제
    threadPool.shutdown()
}

suspend fun suspendFunction(name: String) {
    println("$name: started on ${Thread.currentThread().name}")

    delay(1000)  // 1초 동안 중단됨
    println("After delay in $name on ${Thread.currentThread().name}")

    delay(1000)  // 다시 1초 동안 중단됨
    println("$name: ended on ${Thread.currentThread().name}")
}