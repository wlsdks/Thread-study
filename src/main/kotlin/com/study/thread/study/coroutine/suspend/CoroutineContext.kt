package com.study.thread.study.coroutine.suspend

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// 코루틴 컨텍스트와 일반 함수 컨텍스트 비교
fun main(): Unit = runBlocking {
    launch(Dispatchers.Default) {
        performOperation()
    }
}

// suspend 함수
suspend fun performOperation() {
    println("코루틴 컨텍스트: ${Thread.currentThread().name}")
    regularFunctionContext()
}

// 일반 함수
fun regularFunctionContext() {
    println("일반 함수 컨텍스트: ${Thread.currentThread().name}")
}