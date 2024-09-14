package com.study.thread.study;

import kotlinx.coroutines.delay
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.sin

// 테스트할 작업의 수
const val TASK_COUNT = 300000
const val FILE_COUNT = 1000
const val THREAD_POOL_SIZE = 200
const val DELAY_MS = 10L // I/O 작업을 시뮬레이션하기 위한 지연 시간

// 파일 작업을 시뮬레이션하는 함수 (일반 함수)
fun simulateFileOperation(fileName: String) {
    Thread.sleep(DELAY_MS) // I/O 작업 시뮬레이션
}

// 파일 작업을 시뮬레이션하는 함수 (코루틴 전용)
suspend fun simulateFileOperationCoroutine(fileName: String) {
    delay(DELAY_MS) // I/O 작업 시뮬레이션
}

// 차단 작업을 시뮬레이션하는 함수 (가상 스레드 전용)
fun simulateVirtualThreadFileOperation(fileName: String) {
    val path = Paths.get(fileName)
    // 파일을 읽는 차단 작업
    Files.readAllBytes(path)  // 실제 파일 읽기 작업
    Thread.sleep(30)  // I/O 작업 지연 시뮬레이션
}

// CPU 집중 작업을 시뮬레이션하는 함수 (모든 모델 공통)
fun simulateCPUIntensiveOperation() {
    var result = 0.0
    for (i in 1..1_000_000) {
        result += sin(i.toDouble())
    }
}

// 활성화된 스레드 수를 계산하는 함수 (일반 , 스레드 풀)
fun countActiveThreads(createdThreads: Set<Long>): Int {
    return Thread.getAllStackTraces().keys.count { it.id in createdThreads }
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