package akka.kotlin.classic.example6

import akka.actor.ActorRefFactory
import akka.actor.Props
import akka.kotlin.classic.ChannelActor
import akka.kotlin.classic.exampleshared.GetCounter
import akka.kotlin.classic.exampleshared.counterResponse
import akka.kotlin.classic.exampleshared.getCounterAsync
import akka.kotlin.classic.runAkka
import akka.kotlin.classic.startReceive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class DemoKotlinActor: ChannelActor<GetCounter>() {
    init {
        startReceive {
            var counter = 0
            while(!inbox.isClosedForReceive){
                val msg = inbox.receive()
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
        // send requests in random order to verify
        launch {
            delay(Random.nextLong(100))
            val response = channelActor.getCounterAsync(i)
            System.err.println("sent $i received ${response.await().seqNr}")
        }
    }

    delay(5000)
}

suspend fun slowService(request: Int): Int{
    delay(50)
    return request
}
//fun ActorRef.counterResponse(seqNr: Int, counter: Int) = tell(CounterResponse(seqNr = seqNr, counter = counter), null)