package akka.kotlin.classic.example2

import akka.actor.Props
import akka.kotlin.classic.ChannelActor
import akka.kotlin.classic.exampleshared.GetCounter
import akka.kotlin.classic.exampleshared.counterResponse
import akka.kotlin.classic.exampleshared.getCounterAsync
import akka.kotlin.classic.runAkka
import akka.pattern.Patterns.gracefulStop
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.seconds
import kotlin.time.toJavaDuration

class DemoKotlinActor : ChannelActor<GetCounter>() {
    init {
        launch {
            var counter = 0
            for (msg in channel) {
                counter += 1

                val response = slowService(msg.msg.seqNr)

                msg.sender.counterResponse(seqNr = response, counter = counter)
            }
        }
    }

    override fun postStop() {
        System.err.println("actor stopped")
    }

    companion object {
        fun props() = Props.create { DemoKotlinActor() }
    }
}

@OptIn(ExperimentalTime::class)
fun main() = runAkka {
    actorSystem.registerOnTermination { System.err.println("actor system terminating") }

    val channelActor = actorSystem.actorOf(DemoKotlinActor.props())

    for (i in 1..10) {
        // send requests in random order to verify
        launch {
            delay(Random.nextLong(100))
            val response = channelActor.getCounterAsync(i, 3000)
            System.err.println("sent $i received ${response.await().seqNr}")
        }
    }

    delay(5000)
}

suspend fun slowService(request: Int): Int {
    //throw Exception()
    delay(1000)
    return request
}
