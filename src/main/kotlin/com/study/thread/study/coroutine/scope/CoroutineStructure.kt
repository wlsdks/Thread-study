package com.study.thread.study.coroutine.scope


import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    try {
        coroutineScope {
            launch {
                println("작업 1 시작")
                delay(1000)
                println("작업 1 완료")
            }
            launch {
                println("작업 2 시작")
                delay(500)
                throw Exception("작업 2에서 예외 발생")
            }
        }
    } catch (e: Exception) {
        println("예외 처리: ${e.message}")
    }
    println("메인 함수 종료")
}