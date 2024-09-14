package com.study.thread.study

import kotlinx.coroutines.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis


fun main() = runBlocking {
    println("📊 동시성 모델별 락 경합 성능 비교 테스트")
    println("이 테스트는 여러 스레드가 동시에 하나의 공유 자원(counter)에 접근할 때의 성능을 비교합니다.")
    println("총 작업 수: $TASK_COUNT")
    println("스레드 풀 크기: $THREAD_POOL_SIZE")

    testLockContentionThreads()
    testLockContentionThreadPool()
    testLockContentionVirtualThreads()
    testLockContentionCoroutines()
    testLockContentionAtomicInteger()
}

// 공유 자원: 모든 스레드가 접근하여 증가시킬 카운터
var counter = 0
// 동기화를 위한 락 객체
val lock = Any()
// 락 없이 동시성을 제어하기 위한 AtomicInteger
val atomicCounter = AtomicInteger(0)

// 테스트 전 카운터 초기화 함수
fun resetCounters() {
    counter = 0
    atomicCounter.set(0)
}

// 일반 스레드 락 경합 테스트
fun testLockContentionThreads() {
    println("\n🧵 일반 스레드 락 경합 테스트 시작")
    println("이 테스트는 각 작업마다 새로운 스레드를 생성하여 카운터를 증가시킵니다.")
    resetCounters()

    val threadTime = measureTimeMillis {
        val threads = List(TASK_COUNT) {
            Thread {
                synchronized(lock) { // 락을 사용하여 동기화
                    counter++
                }
            }.apply { start() } // Thread 객체를 생성한 후 즉시 그 스레드를 시작
        }
        threads.forEach { it.join() } // 모든 스레드가 종료될 때까지 대기
    }

    println("📊 최종 카운터 값: $counter")
    println("🧵 일반 스레드 락 경합 실행 시간: ${threadTime}ms")
    println("📈 초당 처리량: ${"%.2f".format(TASK_COUNT.toDouble() / (threadTime / 1000.0))} 작업/초")
}

// 스레드 풀 락 경합 테스트
fun testLockContentionThreadPool() {
    println("\n🧵 스레드 풀 락 경합 테스트 시작")
    println("이 테스트는 고정 크기의 스레드 풀을 사용하여 작업을 처리합니다.")
    resetCounters()

    val executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)

    val threadTime = measureTimeMillis {
        val futures = List(TASK_COUNT) {
            executor.submit {
                synchronized(lock) { // 락을 사용하여 동기화
                    counter++
                }
            }
        }
        futures.forEach { it.get() } // 모든 작업이 완료될 때까지 대기
    }

    executor.shutdown()
    executor.awaitTermination(1, TimeUnit.MINUTES)

    println("📊 최종 카운터 값: $counter")
    println("🧵 스레드 풀 락 경합 실행 시간: ${threadTime}ms")
    println("📈 초당 처리량: ${"%.2f".format(TASK_COUNT.toDouble() / (threadTime / 1000.0))} 작업/초")
}

// 가상 스레드 락 경합 테스트
fun testLockContentionVirtualThreads() {
    println("\n🪶 가상 스레드 락 경합 테스트 시작")
    println("이 테스트는 Java 21의 가상 스레드를 사용하여 작업을 처리합니다.")
    resetCounters()

    // 스레드풀 방식으로 다수의 가상 스레드를 관리
    val executor = Executors.newVirtualThreadPerTaskExecutor()

    val virtualThreadTime = measureTimeMillis {
        val futures = List(TASK_COUNT) {
            executor.submit {
                synchronized(lock) { // 락을 사용하여 동기화
                    counter++
                }
            }
        }
        futures.forEach { it.get() } // 모든 작업이 완료될 때까지 대기
        executor.close()
    }

    println("📊 최종 카운터 값: $counter")
    println("🪶 가상 스레드 락 경합 실행 시간: ${virtualThreadTime}ms")
    println("📈 초당 처리량: ${"%.2f".format(TASK_COUNT.toDouble() / (virtualThreadTime / 1000.0))} 작업/초")
}

// 코루틴 락 경합 테스트
suspend fun testLockContentionCoroutines() = coroutineScope {
    println("\n🚀 코루틴 락 경합 테스트 시작")
    println("이 테스트는 Kotlin 코루틴을 사용하여 작업을 처리합니다.")
    resetCounters()

    val coroutineTime = measureTimeMillis {
        val jobs = List(TASK_COUNT) {
            launch(Dispatchers.Default) {
                synchronized(lock) { // 락을 사용하여 동기화
                    counter++
                }
            }
        }
        jobs.forEach { it.join() } // 모든 코루틴이 완료될 때까지 대기
    }

    println("📊 최종 카운터 값: $counter")
    println("🚀 코루틴 락 경합 실행 시간: ${coroutineTime}ms")
    println("📈 초당 처리량: ${"%.2f".format(TASK_COUNT.toDouble() / (coroutineTime / 1000.0))} 작업/초")
}

// AtomicInteger를 사용한 락 없는 동시성 테스트
fun testLockContentionAtomicInteger() {
    println("\n⚛️ AtomicInteger 동시성 테스트 시작")
    println("이 테스트는 락 대신 AtomicInteger를 사용하여 동시성을 제어합니다.")
    resetCounters()

    val atomicTime = measureTimeMillis {
        val executor = Executors.newVirtualThreadPerTaskExecutor()
        val futures = List(TASK_COUNT) {
            executor.submit {
                atomicCounter.incrementAndGet() // 락 없이 안전하게 증가
            }
        }
        futures.forEach { it.get() } // 모든 작업이 완료될 때까지 대기
        executor.close()
    }

    println("📊 최종 AtomicInteger 값: ${atomicCounter.get()}")
    println("⚛️ AtomicInteger 실행 시간: ${atomicTime}ms")
    println("📈 초당 처리량: ${"%.2f".format(TASK_COUNT.toDouble() / (atomicTime / 1000.0))} 작업/초")
}