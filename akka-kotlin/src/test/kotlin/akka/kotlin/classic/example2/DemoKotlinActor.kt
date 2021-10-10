package akka.kotlin.classic.example2

import akka.kotlin.classic.ChannelActor
import akka.kotlin.classic.Props
import akka.kotlin.classic.exampleshared.GetCounter
import akka.kotlin.classic.exampleshared.counterResponse
import akka.kotlin.classic.exampleshared.getCounterAsync
import akka.kotlin.classic.runAkka
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

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

    companion object {
        fun props() = Props.create { DemoKotlinActor() }
    }
}

fun main() = runAkka {
    actorSystem.registerOnTermination { System.err.println("actor system terminating") }

    val channelActor = actorSystem.actorOf(DemoKotlinActor.props())

    for (i in 1..10) {
        // send requests in random order to verify concurrency
        launch {
            delay(Random.nextLong(100))
            val response = channelActor.getCounterAsync(i, 6000)
            System.err.println("sent $i received ${response.await().seqNr}")
        }
    }

    delay(5000)
}

suspend fun slowService(request: Int): Int {
    //throw Exception()
    delay(500)
    return request
}
