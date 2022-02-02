package akka.kotlin.classic.local

import akka.actor.AbstractActor
import akka.kotlin.classic.Props
import akka.kotlin.classic.*
import akka.kotlin.classic.match
import akka.kotlin.shared.GetCounter
import akka.kotlin.shared.IncCounter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay

/** kotlin actors send a CompletableDeferred to an actor for an answer. This won't work with an Akka cluster. */
fun main() = runAkka {

    val demoActor = actorSystem.actorOf(CounterActor.props())
    val response = CompletableDeferred<Int>()

    demoActor.tell(IncCounter)
    demoActor.tell(GetCounter(response))

    val answer = response.await()

    println("received $answer")
    delay(5000)
}

internal class CounterActor : AbstractActor() {
    var counter = 0

    override fun createReceive(): Receive =
        match<GetCounter> { it.response.complete(counter) }
            .match<IncCounter> { counter+= 1 }
            .build()

    companion object {
        fun props() = Props.create { CounterActor() }
    }
}
