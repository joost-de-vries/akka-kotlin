package akka.kotlin.standardKotlin

import akka.actor.AbstractActor
import akka.kotlin.shared.CounterMsg
import akka.kotlin.shared.GetCounter
import akka.kotlin.shared.IncCounter
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.system.*

suspend fun massiveRun(action: suspend () -> Unit) {
    val n = 100  // number of coroutines to launch
    val k = 1000 // times an action is repeated by each coroutine
    val time = measureTimeMillis {
        coroutineScope { // scope for coroutines
            repeat(n) {
                launch {
                    repeat(k) { action() }
                }
            }
        }
    }
    println("Completed ${n * k} actions in $time ms")
}


// This function launches a new counter actor
fun CoroutineScope.counterActor() = actor<CounterMsg> {
    var counter = 0 // actor state
    for (msg in channel) { // iterate over incoming messages
        when (msg) {
            is IncCounter -> counter++
            is GetCounter -> msg.response.complete(counter)
        }
    }
}

fun main() = runBlocking<Unit> {
    val counter = counterActor() // create the actor
    withContext(Dispatchers.Default) {
        massiveRun {
            counter.send(IncCounter)
        }
    }
    // send a message to get a counter value from an actor
    val response = CompletableDeferred<Int>()
    counter.send(GetCounter(response))
    println("Counter = ${response.await()}")
    counter.close()
}

public fun <E> CoroutineScope.akkaActor(
    context: CoroutineContext = EmptyCoroutineContext
    ,block: suspend ActorScope<E>.() -> Unit
): SendChannel<E> {
    // create akka actor that forwards to channel
    // create channel that sends to the actor
    // send return messages ?? like in the akka actor api that returns Future[A]
    val newContext = newCoroutineContext(context)
    val channel = Channel<E>() // actors already have an inbox, so no extra capacity in this channel
    val akkaActor = object: AbstractActor() {
        override fun createReceive(): Receive{
            this.context.dispatcher.asCoroutineDispatcher()
            return receiveBuilder()
                .matchUnchecked(this.javaClass){ msg -> async { channel.send(msg as E)} }
                .build()

        }


    }

    return channel
}

