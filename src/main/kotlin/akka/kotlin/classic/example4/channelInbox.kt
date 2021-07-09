package akka.kotlin.classic.example4

import akka.actor.ActorRefFactory
import akka.kotlin.classic.Props
import akka.kotlin.classic.ChannelActor
import akka.kotlin.classic.exampleshared.GetCounter
import akka.kotlin.classic.exampleshared.counterResponse
import akka.kotlin.classic.exampleshared.getCounter
import akka.kotlin.classic.exampleshared.getCounterAsync
import akka.kotlin.classic.runAkka
import akka.kotlin.classic.startReceive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** an actor with a ReceiveChannel inbox. It remains responsive to system messages while suspending on a slow service call. */
class DemoKotlinActor: ChannelActor<GetCounter>() {
    init {
        startReceive {
            var counter = 0
            for(msg in inbox){
                counter += 1

                val response = slowService(msg.seqNr)

                sender.counterResponse(seqNr = response, counter = counter)
            }
        }
    }
}

fun ActorRefFactory.demoActor() = actorOf(demoActorProps())
fun demoActorProps() = Props.create { DemoKotlinActor() }

fun main() = runAkka {
    actorSystem.registerOnTermination { System.err.println("actor system terminating") }

    val channelActor = actorSystem.demoActor()

    for(i in 1..10){
        // send requests in random order to verify concurrency
        launch {
            delay(Random.nextLong(100))
            val response = channelActor.getCounter(i)
            System.err.println("sent $i received ${response.seqNr}")
        }
    }

    delay(5000)
}

suspend fun slowService(request: Int): Int{
    delay(50)
    return request
}
