package com.study.thread.study.coroutine.scope

import kotlinx.coroutines.*

fun main() = runBlocking {
    // 이 블록 내에서 코루틴 스코프가 생성됩니다.
    launch {
        println("코루틴 1 실행 중")
    }
    launch {
        println("코루틴 2 실행 중")
    }
    // runBlocking은 내부의 모든 코루틴이 완료될 때까지 현재 스레드를 차단합니다.
    println("메인 함수 종료")
}