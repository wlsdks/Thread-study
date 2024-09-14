package com.study.thread.study;

import kotlinx.coroutines.*
import java.lang.management.ManagementFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis


fun main() = runBlocking {
    testIOIntensiveCoroutines()
//    testCPUIntensiveCoroutines()
}

// 파일 작업을 시뮬레이션하는 함수 (코루틴 전용)
suspend fun simulateFileOperationCoroutine(fileName: String) {
    delay(DELAY_MS) // I/O 작업 시뮬레이션
}

// 코루틴 테스트 수정
suspend fun testIOIntensiveCoroutines() = coroutineScope {
    println("\n🚀 코루틴 I/O 집중 작업 테스트 시작")
    val threadMXBean = ManagementFactory.getThreadMXBean()
    val startThreadCount = threadMXBean.threadCount
    println("🛫 시작 시 활성 스레드 수: $startThreadCount")

    val tasksCompleted = AtomicInteger(0)
    val threadSet = ConcurrentHashMap.newKeySet<Thread>()
    val peakThreadCount = AtomicInteger(0)

    val coroutineTime = measureTimeMillis {
        val jobs = List(TEST_COUNT) {
            launch(Dispatchers.IO) {
                val currentThread = Thread.currentThread()
                threadSet.add(currentThread)
                peakThreadCount.updateAndGet { maxOf(it, threadSet.size) }

                simulateFileOperationCoroutine("coroutine_file_$it.txt")
                tasksCompleted.incrementAndGet()
            }
        }
        jobs.joinAll()
    }

    val endThreadCount = threadMXBean.threadCount
    println("🛬 종료 시 활성 스레드 수: $endThreadCount")
    println("📊 생성된 코루틴 수: $TEST_COUNT")
    println("📊 사용된 고유 스레드(OS 수준의 스레드) 수: ${threadSet.size}")
    println("📊 최대 동시 활성 스레드 수: ${peakThreadCount.get()}")
    println("📊 완료된 작업 수: ${tasksCompleted.get()}")
    println("🚀 코루틴 I/O 작업 실행 시간: ${coroutineTime}ms")
}

// 코루틴 CPU 집중 작업 테스트
suspend fun testCPUIntensiveCoroutines() = coroutineScope {
    println("\n🚀 코루틴 CPU 집중 작업 테스트 시작")
    val threadMXBean = ManagementFactory.getThreadMXBean()
    val startThreadCount = threadMXBean.threadCount
    println("🛫 시작 시 활성 스레드 수: $startThreadCount")

    val tasksCompleted = AtomicInteger(0)
    val threadSet = ConcurrentHashMap.newKeySet<Thread>()
    val peakThreadCount = AtomicInteger(0)

    val coroutineTime = measureTimeMillis {
        val jobs = List(TEST_COUNT) {
            launch(Dispatchers.Default) {  // Default 디스패처 사용 (CPU 작업에 적합)
                val currentThread = Thread.currentThread()
                threadSet.add(currentThread)
                peakThreadCount.updateAndGet { maxOf(it, threadSet.size) }

                simulateCPUIntensiveOperation()
                tasksCompleted.incrementAndGet()
            }
        }
        jobs.joinAll()
    }

    val endThreadCount = threadMXBean.threadCount
    println("🛬 종료 시 활성 스레드 수: $endThreadCount")
    println("📊 생성된 코루틴 수: $TEST_COUNT")
    println("📊 사용된 고유 스레드(OS 수준의 스레드) 수: ${threadSet.size}")
    println("📊 최대 동시 활성 스레드 수: ${peakThreadCount.get()}")
    println("📊 완료된 작업 수: ${tasksCompleted.get()}")
    println("🚀 코루틴 CPU 작업 실행 시간: ${coroutineTime}ms")
    println("📊 초당 처리된 작업 수: ${"%.2f".format(TEST_COUNT * 1000.0 / coroutineTime)}")
}