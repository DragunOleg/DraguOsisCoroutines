package com.example.draguosiscoroutines.samples

import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.concurrent.thread

fun main () {
    six()
}
private fun one() = runBlocking { // coroutine builder.
    launch { // coroutine builder. запускает корутину и продолжает выполнение
        delay(3000L) // suspending function. Откладывает корутину на 3 с
        println("World!") // выводится после делэя
    }
    println("Hello")
}

private fun two() = runBlocking {
    doWorld()
}

private suspend fun doWorld() = coroutineScope {  // this: CoroutineScope
    launch {
        delay(1000L)
        println("World!")
    }
    println("Hello")
}

// Последовательно выполняет doWorld с "Done" после
private fun three() = runBlocking {
    doWorld2()
    println("Done")
}

// Параллельно выполняет обе секции
private suspend fun doWorld2() = coroutineScope { // this: CoroutineScope
    launch {
        delay(2000L)
        println("World 2")
    }
    launch {
        delay(1000L)
        println("World 1")
    }
    println("Hello")
}
/*result: Hello\n World 1\n World 2\n Done */


private fun four() = runBlocking {
    val job = launch { // запускаем корутину и сохраняем ссылку на job
        delay(1000L)
        println("World!")
    }
    println("Hello")
    job.join() // ожидаем выполнения job
    println("Done")
}
/*вывод Hello\n World!\n Done*/


private fun five() {
    val startTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    println("start time: $startTime")
    runBlocking {
        repeat(100_000) { // launch a lot of coroutines
            launch {
                delay(5000L)
                print(".")
            }
        }
    }
    val diff = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - startTime
    println("diff =: $diff")
}

private fun six() {
    val startTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    println("start time: $startTime")
    repeat(100_000) { // try to repeat with threads
        thread {
            Thread.sleep(5000L)
            print(".")
        }
    }
    val diff = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - startTime
    println("diff =: $diff")
}



