package com.study.thread.study.coroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    launch(Dispatchers.Default) {  // 첫번째 코루틴
        suspendFunction2()
    }

    launch(Dispatchers.Default) {  // 두번째 코루틴
        suspendFunction2()
    }

    launch(Dispatchers.Default) {  // 세번째 코루틴
        suspendFunction2()
    }

    launch(Dispatchers.Default) {  // 네번째 코루틴
        repeat(5) {
            delay(300)  // 0.3초마다 출력
            println("Other coroutine running: $it on ${Thread.currentThread().name}")
        }
    }
}

suspend fun suspendFunction2() {
    println("Suspend function started on ${Thread.currentThread().name}")

    delay(1000)  // 1초 동안 중단됨
    println("After delay in suspend function on ${Thread.currentThread().name}")

    delay(1000)  // 다시 1초 동안 중단됨
    println("Suspend function ended on ${Thread.currentThread().name}")
}