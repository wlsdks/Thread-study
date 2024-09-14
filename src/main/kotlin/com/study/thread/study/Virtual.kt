package com.study.thread.study;

import kotlinx.coroutines.*
import java.lang.management.ManagementFactory
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

fun main() {
    println("📊 스레드, 코루틴, 가상 스레드 성능 비교 테스트 (파일 읽기/쓰기 시뮬레이션)")

//    createTestFiles() // 테스트 파일 생성
    testIOIntensiveVirtualThreadsWithOptimizedMetrics()
//    testCPUIntensiveVirtualThreadsWithOptimizedMetrics()
}

// 가상 스레드 I/O 집중 작업 테스트 (최적화된 메트릭스)
fun testIOIntensiveVirtualThreadsWithOptimizedMetrics() {
    println("\n🪶 가상 스레드 I/O 집중 작업 테스트 시작 (최적화된 메트릭스)")

    val threadMXBean = ManagementFactory.getThreadMXBean()
    val tasksCompleted = AtomicInteger(0)
    val virtualThreadCounter = AtomicInteger(0)
    val peakActiveThreads = AtomicInteger(0)
    val isHalfwayPoint = AtomicBoolean(false)

    val startThreadCount = threadMXBean.threadCount
    println("🛫 시작 시 활성 스레드 수: $startThreadCount")

    // 비동기 덤프 생성을 위한 코루틴
    val dumpJob = CoroutineScope(Dispatchers.IO).launch {
        dumpThreadsToFile("start_dump.json")
        while (isActive) {
            delay(100) // 100ms마다 체크
            if (isHalfwayPoint.get()) {
                dumpThreadsToFile("mid_dump.json")
                break
            }
        }
    }

    val virtualThreadTime = measureTimeMillis {
        val executor = Executors.newVirtualThreadPerTaskExecutor()
        val tasks = List(FILE_COUNT) {
            executor.submit {
                if (Thread.currentThread().isVirtual) {
                    virtualThreadCounter.incrementAndGet()
                }
                peakActiveThreads.updateAndGet { maxOf(it, threadMXBean.threadCount) }

                // 만약 실제 io가 없으면 활성 스레드가 훨씬 적게 나올 것이다.
                simulateVirtualThreadFileOperation("testfile_0.txt")

                val completed = tasksCompleted.incrementAndGet()
                if (completed == FILE_COUNT / 2) {
                    isHalfwayPoint.set(true)
                }
            }
        }
        tasks.forEach { it.get() }
        executor.close()
    }

    // 덤프 생성 코루틴 종료
    runBlocking {
        dumpJob.cancelAndJoin()
    }

    // 마지막 덤프 생성
    dumpThreadsToFile("end_dump.json")

    val endThreadCount = threadMXBean.threadCount

    println("\n📊 테스트 결과:")
    println("🛬 종료 시 활성 스레드 수: $endThreadCount")
    println("📊 생성된 가상 스레드 총 수: ${virtualThreadCounter.get()}")
    println("📊 최대 동시 활성 스레드 수: ${peakActiveThreads.get()}")
    println("📊 완료된 작업 수: ${tasksCompleted.get()}")
    println("🪶 가상 스레드 I/O 작업 실행 시간: ${virtualThreadTime}ms")
    println("📊 초당 처리된 작업 수: ${"%.2f".format(FILE_COUNT * 1000.0 / virtualThreadTime)}")
}


// CPU 집중 작업을 시뮬레이션하는 함수
fun testCPUIntensiveVirtualThreadsWithOptimizedMetrics() {
    println("\n🪶 가상 스레드 CPU 집중 작업 테스트 시작 (최적화된 메트릭스)")

    val threadMXBean = ManagementFactory.getThreadMXBean()
    val tasksCompleted = AtomicInteger(0)
    val virtualThreadCounter = AtomicInteger(0)
    val peakActiveThreads = AtomicInteger(0)
    val isHalfwayPoint = AtomicBoolean(false)

    val startThreadCount = threadMXBean.threadCount
    println("🛫 시작 시 활성 스레드 수: $startThreadCount")

    // 비동기 덤프 생성을 위한 코루틴
    val dumpJob = CoroutineScope(Dispatchers.Default).launch {
        dumpThreadsToFile("cpu_start_dump.json")
        while (isActive) {
            delay(100) // 100ms마다 체크
            if (isHalfwayPoint.get()) {
                dumpThreadsToFile("cpu_mid_dump.json")
                break
            }
        }
    }

    val virtualThreadTime = measureTimeMillis {
        val executor = Executors.newVirtualThreadPerTaskExecutor()
        val tasks = List(FILE_COUNT) {
            executor.submit {
                if (Thread.currentThread().isVirtual) {
                    virtualThreadCounter.incrementAndGet()
                }
                peakActiveThreads.updateAndGet { maxOf(it, threadMXBean.threadCount) }

                // CPU 집중 작업 시뮬레이션
                simulateCPUIntensiveOperation()

                val completed = tasksCompleted.incrementAndGet()
                if (completed == FILE_COUNT / 2) {
                    isHalfwayPoint.set(true)
                }
            }
        }
        tasks.forEach { it.get() }
        executor.close()
    }

    // 덤프 생성 코루틴 종료
    runBlocking {
        dumpJob.cancelAndJoin()
    }

    // 마지막 덤프 생성
    dumpThreadsToFile("cpu_end_dump.json")

    val endThreadCount = threadMXBean.threadCount

    println("\n📊 테스트 결과:")
    println("🛬 종료 시 활성 스레드 수: $endThreadCount")
    println("📊 생성된 가상 스레드 총 수: ${virtualThreadCounter.get()}")
    println("📊 최대 동시 활성 스레드 수: ${peakActiveThreads.get()}")
    println("📊 완료된 작업 수: ${tasksCompleted.get()}")
    println("🪶 가상 스레드 CPU 작업 실행 시간: ${virtualThreadTime}ms")
    println("📊 초당 처리된 작업 수: ${"%.2f".format(FILE_COUNT * 1000.0 / virtualThreadTime)}")
}