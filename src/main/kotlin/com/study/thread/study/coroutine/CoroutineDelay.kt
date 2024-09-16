package com.study.thread.study.coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    launch {
        println("코루틴 1: 시작")
        delay(1000) // 1초 동안 중단
        println("코루틴 1: 1초 후 재개")
    }

    launch {
        println("코루틴 2: 시작")
        delay(500) // 0.5초 동안 중단
        println("코루틴 2: 0.5초 후 재개")
        delay(700) // 추가 0.7초 동안 중단
        println("코루틴 2: 추가 0.7초 후 재개")
    }

    println("메인: 모든 코루틴 시작됨")
    delay(2000) // 메인 코루틴을 2초 동안 중단하여 다른 코루틴들이 완료될 때까지 기다림
    println("메인: 종료")
}