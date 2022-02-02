package akka.kotlin.classic.example1

import akka.actor.ActorRefFactory
import akka.kotlin.classic.Props
import akka.kotlin.classic.ChannelActorMessage
import akka.kotlin.classic.ChannelActorSuspend
import akka.kotlin.classic.exampleshared.GetCounter
import akka.kotlin.classic.exampleshared.counterResponse
import akka.kotlin.classic.exampleshared.getCounter
import akka.kotlin.classic.runAkka
import kotlinx.coroutines.delay

class DemoKotlinActor: ChannelActorSuspend<GetCounter>() {
    var counter = 0

    override suspend fun receiveSuspend(msg: ChannelActorMessage<GetCounter>) {
        counter += 1
        val response = slowService(msg.msg.seqNr)
        msg.sender.counterResponse(seqNr = response, counter=counter)
    }

    companion object {
        fun props(name: String) = Props.create { DemoKotlinActor() }
    }
}
fun ActorRefFactory.demoKotlinActor(name: String) = actorOf(DemoKotlinActor.props(name))

suspend fun slowService(request: Int): Int{
    delay(50)
    return request
}

fun main() = runAkka {
    val channelActor = actorSystem.demoKotlinActor("carl")

    for(i in 1..10){
        val response = channelActor.getCounter(i, timeout = 3000)
        System.err.println("sent $i received ${response.seqNr}")
    }

    actorSystem.registerOnTermination { System.err.println("actor system terminating") }
    delay(5000)
}
