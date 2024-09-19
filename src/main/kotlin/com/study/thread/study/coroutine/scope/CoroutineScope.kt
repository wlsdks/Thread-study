package com.study.thread.study.coroutine.scope

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

suspend fun main() {
    println("메인 함수 시작")
    coroutineScope {
        // 이 블록 내에서 새로운 코루틴 스코프가 생성됩니다.
        launch {
            println("코루틴 1 실행 중")
            delay(1000)
            println("코루틴 1 완료")
        }
        launch {
            println("코루틴 2 실행 중")
            delay(500)
            println("코루틴 2 완료")
        }
        println("코루틴 스코프 내의 코드 실행")
    }
    println("메인 함수 종료")
}