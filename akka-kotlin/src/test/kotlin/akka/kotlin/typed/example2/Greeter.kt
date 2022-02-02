package akka.kotlin.typed.example2

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import akka.kotlin.typed.onMessage

sealed class Salutation()
data class Greet(val whom: String, val replyTo: ActorRef<SalutationResponse>) : Salutation()
data class DamnYou(val whom: String, val replyTo: ActorRef<SalutationResponse>) : Salutation()
fun DamnYou(whom:String): (ActorRef<SalutationResponse>) -> DamnYou = { DamnYou(whom,it) }

sealed class SalutationResponse()
data class Greeted(val whom: String, val from: ActorRef<Salutation>) : SalutationResponse()
data class DamnYouToo(val whom: String, val from: ActorRef<Salutation>) : SalutationResponse()

class Greeter private constructor(context: ActorContext<Salutation>) : AbstractBehavior<Salutation>(context) {

    override fun createReceive(): Receive<Salutation> = newReceiveBuilder()
        .onMessage(::onGreet)
        .onMessage(::onDamnYou)
        .build()
    fun createReceive2(): Receive<Salutation> = newReceiveBuilder()
        .onMessage{ when(it){
            is Greet -> {
                context.log.info("Hello {}!", it.whom)
                it.replyTo.tell(Greeted(it.whom, context.self))
                this
            }
            is DamnYou -> this
        } }
        .build()

    private fun onGreet(command: Greet): Behavior<Salutation> {
        context.log.info("Hello {}!", command.whom)
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

