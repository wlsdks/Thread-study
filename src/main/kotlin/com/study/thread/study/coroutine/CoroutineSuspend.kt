package com.study.thread.study.coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun main(): Unit = runBlocking {
    launch {  // 첫 번째 코루틴
        suspendFunction()
    }

    launch {  // 두 번째 코루틴
        repeat(5) {
            delay(300)  // 0.3초마다 출력
            println("Other coroutine running: $it")
        }
    }
}

suspend fun suspendFunction() {
    println("Suspend function started")

    delay(1000)  // 1초 동안 중단됨
    println("After delay in suspend function")

    delay(1000)  // 다시 1초 동안 중단됨
    println("Suspend function ended")
}