package com.study.thread.study.coroutine.scope

import kotlinx.coroutines.*

class MyClass : CoroutineScope {
    private val job = Job()
    override val coroutineContext = Dispatchers.Default + job

    fun start() {
        launch {
            println("작업 시작")
            delay(1000)
            println("작업 완료")
        }
    }

    fun stop() {
        job.cancel()
    }
}

fun main() {
    val myClass = MyClass()
    myClass.start()
    Thread.sleep(1500)
    myClass.stop()
}