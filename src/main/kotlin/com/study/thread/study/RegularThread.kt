package com.study.thread.study;

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis


fun main() {
    val regularThreadIoResult = testIOIntensiveThreads()
    printTestResult(regularThreadIoResult)

    val regularThreadCpuResult = testCPUIntensiveThreads()
    printTestResult(regularThreadCpuResult)
}

// 파일 작업을 시뮬레이션하는 함수 (일반 함수)
fun simulateFileOperation(fileName: String) {
    Thread.sleep(DELAY_MS) // I/O 작업 시뮬레이션
}


// 일반 스레드 I/O 집중 작업 테스트
fun testIOIntensiveThreads(): TestResult {
    val initialThreadCount = Thread.activeCount()
    val tasksCompleted = AtomicInteger(0)
    val createdThreads = ConcurrentHashMap.newKeySet<Long>()
    val maxActiveThreads = AtomicInteger(0)
    val taskTimes = ConcurrentLinkedQueue<Long>()

    val threadTime = measureTimeMillis {
        val threads = List(FILE_COUNT) {
            Thread {
                createdThreads.add(Thread.currentThread().id)
                maxActiveThreads.updateAndGet { max -> maxOf(max, Thread.activeCount()) }

                val taskStartTime = System.nanoTime()
                simulateFileOperation("thread_file_$it.txt")
                val taskEndTime = System.nanoTime()

                taskTimes.add((taskEndTime - taskStartTime) / 1_000_000)
                tasksCompleted.incrementAndGet()
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }
    }

    Thread.sleep(100) // 스레드 종료 대기

    val finalThreadCount = Thread.activeCount()
    val remainingNewThreads = countActiveThreads(createdThreads)

    return TestResult(
        testName = "일반 스레드 I/O 집중 작업 테스트",
        initialThreadCount = initialThreadCount,
        finalThreadCount = finalThreadCount,
        maxActiveThreads = maxActiveThreads.get(),
        createdThreadsCount = createdThreads.size,
        remainingThreadsCount = remainingNewThreads,
        completedTasks = tasksCompleted.get(),
        executionTime = threadTime,
        avgTaskTime = taskTimes.average(),
        minTaskTime = taskTimes.minOrNull() ?: 0,
        maxTaskTime = taskTimes.maxOrNull() ?: 0
    )
}

// 일반 스레드 CPU 집중 작업 테스트
fun testCPUIntensiveThreads(): TestResult {
    val initialThreadCount = Thread.activeCount()
    val tasksCompleted = AtomicInteger(0)
    val createdThreads = ConcurrentHashMap.newKeySet<Long>()
    val maxActiveThreads = AtomicInteger(0)
    val taskTimes = ConcurrentLinkedQueue<Long>()

    val threadTime = measureTimeMillis {
        val threads = List(FILE_COUNT) {
            Thread {
                createdThreads.add(Thread.currentThread().id)
                maxActiveThreads.updateAndGet { max -> maxOf(max, Thread.activeCount()) }
                val taskStartTime = System.nanoTime()

                simulateCPUIntensiveOperation()

                val taskEndTime = System.nanoTime()
                taskTimes.add((taskEndTime - taskStartTime) / 1_000_000)
                tasksCompleted.incrementAndGet()
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }
    }

    Thread.sleep(100) // 스레드 종료 대기

    val finalThreadCount = Thread.activeCount()
    val remainingNewThreads = countActiveThreads(createdThreads)

    return TestResult(
        testName = "일반 스레드 CPU 집중 작업 테스트",
        initialThreadCount = initialThreadCount,
        finalThreadCount = finalThreadCount,
        maxActiveThreads = maxActiveThreads.get(),
        createdThreadsCount = createdThreads.size,
        remainingThreadsCount = remainingNewThreads,
        completedTasks = tasksCompleted.get(),
        executionTime = threadTime,
        avgTaskTime = taskTimes.average(),
        minTaskTime = taskTimes.minOrNull() ?: 0,
        maxTaskTime = taskTimes.maxOrNull() ?: 0
    )
}