package akka.kotlin.typed.example1

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import akka.kotlin.classic.example1.slowService
import akka.kotlin.typed.onMessage
import akka.kotlin.typed.onMessageSuspend

sealed class Salutation()
data class Greet(val whom: String, val replyTo: ActorRef<SalutationResponse>) : Salutation()
data class DamnYou(val whom: String, val replyTo: ActorRef<SalutationResponse>) : Salutation()

sealed class SalutationResponse()
data class Greeted(val whom: String, val from: ActorRef<Salutation>) : SalutationResponse()
data class DamnYouToo(val whom: String, val from: ActorRef<Salutation>) : SalutationResponse()

class Greeter private constructor(context: ActorContext<Salutation>) : AbstractBehavior<Salutation>(context) {

    override fun createReceive(): Receive<Salutation> = newReceiveBuilder()
        .onMessageSuspend(::onGreet)
        .onMessage(::onDamnYou)
        .build()

    private suspend fun onGreet(command: Greet): Behavior<Salutation> {
        context.log.info("Hello {}!", command.whom)
        slowService(1)
        command.replyTo.tell(Greeted(command.whom, context.self))
        return this
    }

    private fun onDamnYou(command: DamnYou): Behavior<Salutation> {
        context.log.info("Damn you {}!", command.whom)
        command.replyTo.tell(DamnYouToo(command.whom, context.self))
        return this
    }

    companion object {
        fun create(): Behavior<Salutation> = Behaviors.setup { Greeter(it) }
    }
}

