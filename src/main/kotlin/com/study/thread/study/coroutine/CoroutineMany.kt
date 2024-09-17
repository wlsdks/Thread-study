package com.study.thread.study.coroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // 새로운 스레드 풀을 사용하는 디스패처 지정
    launch(Dispatchers.Default) {
        println("코루틴 1: 시작 on ${Thread.currentThread().name}")
        delay(1000) // 1초 동안 중단
        println("코루틴 1: 1초 후 재개 on ${Thread.currentThread().name}")
    }

    launch(Dispatchers.Default) {
        println("코루틴 2: 시작 on ${Thread.currentThread().name}")
        delay(500) // 0.5초 동안 중단
        println("코루틴 2: 0.5초 후 재개 on ${Thread.currentThread().name}")
        delay(700) // 추가 0.7초 동안 중단
        println("코루틴 2: 추가 0.7초 후 재개 on ${Thread.currentThread().name}")
    }

    println("메인: 모든 코루틴 시작됨 on ${Thread.currentThread().name}")
    delay(2000) // 메인 코루틴을 2초 동안 중단하여 다른 코루틴들이 완료될 때까지 기다림
    println("메인: 종료 on ${Thread.currentThread().name}")
}
