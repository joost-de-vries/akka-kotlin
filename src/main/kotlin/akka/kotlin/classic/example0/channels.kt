package akka.kotlin.classic.example0

import akka.actor.*
import akka.kotlin.classic.AkkaActorSystem
import akka.kotlin.classic.actorSystem
import akka.kotlin.classic.example0.DemoActor.Companion.demoActor
import akka.kotlin.classic.runAkka
import kotlinx.coroutines.*


/** Use channels as a way to send messages to an actor and receive them */
fun main() = runAkka {

    actorSystem.registerOnTermination { System.err.println("actor system terminating") }

    val demoActor = actorSystem.demoActor(3)

    val (sendChannel, receiveChannel) = channelFor<Int>(demoActor)
    for (i in (0..20)) {
        sendChannel.send(i)
    }

    delay(5000)

    launch {
        for (msg in receiveChannel) {
            println("answer: $msg")
        }
    }
    delay(5000)
}


internal class DemoActor(private val magicNumber: Int) : AbstractActor() {
    override fun createReceive(): Receive {
        return receiveBuilder()
            .match(
                Integer::class.java
            ) { i ->
                println("actor received $i. sending response from $self to $sender")
                sender.tell(i, self)
            }
            .build()
    }

    companion object {
        fun props(magicNumber: Int) = Props.create(DemoActor::class.java) {
                DemoActor(magicNumber)
            }
        fun ActorRefFactory.demoActor(magicNumber: Int) =
            actorOf(DemoActor.props(3))
    }

}
