package com.study.thread.study

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

fun main() {
//    val threadPoolIoResult = testIOIntensiveThreadPool()
//    printTestResult(threadPoolIoResult)

    val threadPoolCpuResult = testCPUIntensiveThreadPool()
    printTestResult(threadPoolCpuResult)
}

// 스레드 풀 I/O 집중 작업 테스트
fun testIOIntensiveThreadPool(): TestResult {
    val initialThreadCount = Thread.activeCount()
    val tasksCompleted = AtomicInteger(0)
    val createdThreads = ConcurrentHashMap.newKeySet<Long>()
    val maxActiveThreads = AtomicInteger(0)
    val threadUsageCounts = ConcurrentHashMap<Long, AtomicInteger>()
    val taskTimes = ConcurrentLinkedQueue<Long>()

    val executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)

    val threadTime = measureTimeMillis {
        val futures = List(TEST_COUNT) {
            executor.submit {
                val threadId = Thread.currentThread().threadId()
                createdThreads.add(threadId)
                threadUsageCounts.computeIfAbsent(threadId) { AtomicInteger(0) }.incrementAndGet()
                maxActiveThreads.updateAndGet { max -> maxOf(max, Thread.activeCount()) }
                val taskStartTime = System.nanoTime()

                simulateFileOperation("thread_file_$it.txt")

                val taskEndTime = System.nanoTime()
                taskTimes.add((taskEndTime - taskStartTime) / 1_000_000)

                tasksCompleted.incrementAndGet()
            }
        }
        futures.forEach { it.get() }
    }

    // 스레드 풀 종료 전 활성 스레드 수
    val activeThreadsBeforeShutdown = countActiveThreads(createdThreads)

    // 스레드 풀 종료
    executor.shutdown()
    executor.awaitTermination(1, TimeUnit.MINUTES)

    // 스레드 풀 종료 후 활성 스레드 수
    val activeThreadsAfterShutdown = countActiveThreads(createdThreads)

    val finalThreadCount = Thread.activeCount()
    val avgThreadUsage = threadUsageCounts.values.map { it.get() }.average()
    val minThreadUsage = threadUsageCounts.values.map { it.get() }.minOrNull() ?: 0
    val maxThreadUsage = threadUsageCounts.values.map { it.get() }.maxOrNull() ?: 0

    return TestResult(
        testName = "스레드 풀 I/O 집중 작업 테스트 (최대 $THREAD_POOL_SIZE 개)",
        initialThreadCount = initialThreadCount,
        finalThreadCount = finalThreadCount,
        maxActiveThreads = maxActiveThreads.get(),
        createdThreadsCount = createdThreads.size,
        remainingThreadsCount = activeThreadsBeforeShutdown,
        completedTasks = tasksCompleted.get(),
        executionTime = threadTime,
        avgTaskTime = taskTimes.average(),
        minTaskTime = taskTimes.minOrNull() ?: 0,
        maxTaskTime = taskTimes.maxOrNull() ?: 0,
        additionalInfo = mapOf(
            "스레드 풀 크기" to THREAD_POOL_SIZE.toString(),
            "스레드 풀 종료 후 활성 스레드 수" to activeThreadsAfterShutdown.toString(),
            "스레드당 평균 작업 수" to "%.2f".format(avgThreadUsage),
            "스레드당 최소 작업 수" to minThreadUsage.toString(),
            "스레드당 최대 작업 수" to maxThreadUsage.toString()
        )
    )
}

// 스레드 풀 CPU 집중 작업 테스트
fun testCPUIntensiveThreadPool(): TestResult {
    val initialThreadCount = Thread.activeCount()
    val tasksCompleted = AtomicInteger(0)
    val createdThreads = ConcurrentHashMap.newKeySet<Long>()
    val maxActiveThreads = AtomicInteger(0)
    val threadUsageCounts = ConcurrentHashMap<Long, AtomicInteger>()
    val taskTimes = ConcurrentLinkedQueue<Long>()

    val executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)

    val threadTime = measureTimeMillis {
        val futures = List(TEST_COUNT) {
            executor.submit {
                val threadId = Thread.currentThread().threadId()
                createdThreads.add(threadId)
                threadUsageCounts.computeIfAbsent(threadId) { AtomicInteger(0) }.incrementAndGet()
                maxActiveThreads.updateAndGet { max -> maxOf(max, Thread.activeCount()) }
                val taskStartTime = System.nanoTime()

                simulateCPUIntensiveOperation()

                val taskEndTime = System.nanoTime()
                taskTimes.add((taskEndTime - taskStartTime) / 1_000_000)
                tasksCompleted.incrementAndGet()
            }
        }
        futures.forEach { it.get() }
    }

    // 스레드 풀 종료 전 활성 스레드 수
    val activeThreadsBeforeShutdown = countActiveThreads(createdThreads)

    // 스레드 풀 종료
    executor.shutdown()
    executor.awaitTermination(1, TimeUnit.MINUTES)

    // 스레드 풀 종료 후 활성 스레드 수
    val activeThreadsAfterShutdown = countActiveThreads(createdThreads)

    val finalThreadCount = Thread.activeCount()
    val avgThreadUsage = threadUsageCounts.values.map { it.get() }.average()
    val minThreadUsage = threadUsageCounts.values.map { it.get() }.minOrNull() ?: 0
    val maxThreadUsage = threadUsageCounts.values.map { it.get() }.maxOrNull() ?: 0

    return TestResult(
        testName = "스레드 풀 CPU 집중 작업 테스트 (최대 $THREAD_POOL_SIZE 개)",
        initialThreadCount = initialThreadCount,
        finalThreadCount = finalThreadCount,
        maxActiveThreads = maxActiveThreads.get(),
        createdThreadsCount = createdThreads.size,
        remainingThreadsCount = activeThreadsBeforeShutdown,
        completedTasks = tasksCompleted.get(),
        executionTime = threadTime,
        avgTaskTime = taskTimes.average(),
        minTaskTime = taskTimes.minOrNull() ?: 0,
        maxTaskTime = taskTimes.maxOrNull() ?: 0,
        additionalInfo = mapOf(
            "스레드 풀 크기" to THREAD_POOL_SIZE.toString(),
            "스레드 풀 종료 후 활성 스레드 수" to activeThreadsAfterShutdown.toString(),
            "스레드당 평균 작업 수" to "%.2f".format(avgThreadUsage),
            "스레드당 최소 작업 수" to minThreadUsage.toString(),
            "스레드당 최대 작업 수" to maxThreadUsage.toString()
        )
    )
}