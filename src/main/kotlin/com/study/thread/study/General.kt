package com.study.thread.study;

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis


fun main() {
    println("📊 스레드 성능 비교 테스트")

    val regularThreadIoResult = testIOIntensiveThreads()
    printTestResult(regularThreadIoResult)

    val threadPoolIoResult = testIOIntensiveThreadPool()
    printTestResult(threadPoolIoResult)

    /*val regularThreadCpuResult = testCPUIntensiveThreads()
    printTestResult(regularThreadCpuResult)

    val threadPoolCpuResult = testCPUIntensiveThreadPool()
    printTestResult(threadPoolCpuResult)*/
}

data class TestResult(
    val testName: String,
    val initialThreadCount: Int,
    val finalThreadCount: Int,
    val maxActiveThreads: Int,
    val createdThreadsCount: Int,
    val remainingThreadsCount: Int,
    val completedTasks: Int,
    val executionTime: Long,
    val avgTaskTime: Double,
    val minTaskTime: Long,
    val maxTaskTime: Long,
    val additionalInfo: Map<String, String> = emptyMap()
)


fun printTestResult(result: TestResult) {
    println("\n🧵 테스트 결과: ${result.testName}")
    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    println("📊 스레드 정보:")
    println("  🛫 시작 시 활성 스레드 수: ${result.initialThreadCount}")
    println("  🛬 종료 시 활성 스레드 수: ${result.finalThreadCount}")
    println("  📈 최대 동시 활성 스레드 수: ${result.maxActiveThreads}")
    println("  🔢 생성된 스레드 총 수: ${result.createdThreadsCount}")
    println("  🔚 (일반, 스레드 풀) 테스트 후 남아있는 스레드 수: ${result.remainingThreadsCount}")

    println("\n📊 작업 처리 정보:")
    println("  ✅ 완료된 작업 수: ${result.completedTasks}")
    println("  ⏱️ 평균 작업 시간: ${"%.2f".format(result.avgTaskTime)} ms")
    println("  🏎️ 최소 작업 시간: ${result.minTaskTime} ms")
    println("  🐢 최대 작업 시간: ${result.maxTaskTime} ms")

    println("\n📊 성능 지표:")
    println("  ⏳ 총 실행 시간: ${result.executionTime} ms")
    println("  🚀 1초당 처리된 작업 수: ${"%.2f".format(result.completedTasks.toDouble() / (result.executionTime / 1000.0))}")

    if (result.additionalInfo.isNotEmpty()) {
        println("\n📌 추가 정보:")
        result.additionalInfo.forEach { (key, value) ->
            println("  • $key: $value")
        }
    }

    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
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
                simulateFileOperationWithoutSuspend("thread_file_$it.txt")
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
        val futures = List(FILE_COUNT) {
            executor.submit {
                val threadId = Thread.currentThread().id
                createdThreads.add(threadId)
                threadUsageCounts.computeIfAbsent(threadId) { AtomicInteger(0) }.incrementAndGet()
                maxActiveThreads.updateAndGet { max -> maxOf(max, Thread.activeCount()) }

                val taskStartTime = System.nanoTime()
                simulateFileOperationWithoutSuspend("thread_file_$it.txt")
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
        val futures = List(FILE_COUNT) {
            executor.submit {
                val threadId = Thread.currentThread().id
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