package akka.kotlin.classic.example5

import akka.actor.*
import akka.actor.Props
import akka.kotlin.classic.*
import akka.kotlin.classic.exampleshared.GetCounter
import akka.kotlin.classic.exampleshared.counterResponse
import akka.kotlin.classic.exampleshared.getCounterAsync
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class DemoKotlinActor: ChannelActor<GetCounter>() {
    init {
        startReceive {
            var counter = 0
            for(msg in inbox){
                try {
                    counter += 1

                    val response = slowService(msg.seqNr)

                    sender.counterResponse(seqNr = response, counter = counter)
                }catch (e: Exception){
                    sender.respondFailure(e)
                }
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
