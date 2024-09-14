package com.study.thread.study;

import kotlinx.coroutines.*
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

fun main() {
//    createTestFiles() // 테스트 파일 생성
    testIOIntensiveVirtualThreadsWithOptimizedMetrics()
//    testCPUIntensiveVirtualThreadsWithOptimizedMetrics()
}

// 차단 작업을 시뮬레이션하는 함수 (가상 스레드 전용)
fun simulateVirtualThreadFileOperation(fileName: String) {
    val path = Paths.get(fileName)
    // 파일을 읽는 차단 작업
    Files.readAllBytes(path)  // 실제 파일 읽기 작업
    Thread.sleep(30)  // I/O 작업 지연 시뮬레이션
}

// 스레드 덤프를 파일로 저장하는 함수 (가상 스레드 전용)
fun dumpThreadsToFile(filename: String) {
    val pid = ManagementFactory.getRuntimeMXBean().name.split("@")[0] // 현재 JVM 프로세스 ID 가져오기
    val dumpCommand = "jcmd $pid Thread.dump_to_file -format=json $filename"

    try {
        val process = Runtime.getRuntime().exec(dumpCommand)
        process.waitFor()
        println("스레드 덤프가 $filename 파일로 저장되었습니다.")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// 테스트 파일 생성 함수 (가상 스레드 전용)
fun createTestFiles() {
    for (i in 0 until FILE_COUNT) {
        val path = Paths.get("testfile_$i.txt")
        if (!Files.exists(path)) {
            Files.write(path, "Test content for file $i".toByteArray())
        }
    }
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