package akka.kotlin.typed.example1

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import akka.kotlin.typed.onMessage


class GreeterBot private constructor(context: ActorContext<SalutationResponse>, private val max: Int) :
    AbstractBehavior<SalutationResponse>(context) {

    private var greetingCounter = 0

    override fun createReceive(): Receive<SalutationResponse> = newReceiveBuilder()
        .onMessage(::onGreeted)
        .onMessage(::onOffended)
        .build()

    private fun onGreeted(message: Greeted): Behavior<SalutationResponse> {
        greetingCounter++
        context.log.info("Greeting {} for {}", greetingCounter, message.whom)
        return when {
            greetingCounter == max -> Behaviors.stopped()
            else -> {
                message.from.tell(Greet(message.whom, context.self))
                this
            }
        }
    }

    private fun onOffended(message: DamnYouToo): Behavior<SalutationResponse> {
        greetingCounter++
        context.log.info("Cursing {} for {}", greetingCounter, message.whom)
        return when {
            greetingCounter == max -> Behaviors.stopped()
            else -> {
                message.from.tell(DamnYou(message.whom, context.self))
                this
            }
        }
    }

    companion object {
        fun create(max: Int): Behavior<SalutationResponse> =
            Behaviors.setup { GreeterBot(it, max) }
    }
}
