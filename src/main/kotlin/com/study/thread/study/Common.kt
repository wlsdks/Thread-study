package com.study.thread.study;

import kotlin.math.sin

// 테스트할 작업의 수
const val TEST_COUNT = 30000
const val THREAD_POOL_SIZE = 3000
const val DELAY_MS = 10L // I/O 작업을 시뮬레이션하기 위한 지연 시간


// CPU 집중 작업을 시뮬레이션하는 함수 (모든 모델 공통)
fun simulateCPUIntensiveOperation() {
    var result = 0.0
    for (i in 1..1_000_000) {
        result += sin(i.toDouble())
    }
}

// 일반, 스레드 풀 작업 처리 결과를 출력하는 함수
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

// 일반, 스레드 풀 작업 처리 결과를 저장하는 데이터 클래스
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

// 활성화된 스레드 수를 계산하는 함수 (일반 , 스레드 풀)
fun countActiveThreads(createdThreads: Set<Long>): Int {
    return Thread.getAllStackTraces().keys.count { it.id in createdThreads }
}