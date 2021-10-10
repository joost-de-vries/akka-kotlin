package akka.kotlin.classic.example0

import akka.actor.AbstractActor
import akka.actor.ActorRefFactory
import akka.kotlin.classic.*
import akka.kotlin.classic.example0.DemoActor.Companion.demoActor
import kotlinx.coroutines.*


/** Use channels as a way to send messages to an actor and receive responses */
fun main() = runAkka {

    actorSystem.registerOnTermination { System.err.println("actor system terminating") }
    this.coroutineContext[Job]!!.invokeOnCompletion { System.err.println("coroutines terminating") }

    val demoActor = actorSystem.demoActor(3)

    val (sendChannel, receiveChannel) = channelFor<Int>(demoActor)
    for (i in (0..20)) {
        sendChannel.send(i)
    }
    delay(3000)

    val job = launch {
        for (msg in receiveChannel) {
            println("answer: $msg")
        }

        println("receive completed")
    }
    delay(3000)
    job.cancel()
}


internal class DemoActor(private val magicNumber: Int) : AbstractActor() {
    override fun createReceive(): Receive =
            match<Int> { i ->
                println("actor received $i. sending response from $self to $sender")
                sender.tell(i, self)
            }
            .build()

    companion object {
        fun ActorRefFactory.demoActor(magicNumber: Int) = actorOf(
            createProps {
                DemoActor(magicNumber)
            }
        )
    }
}
