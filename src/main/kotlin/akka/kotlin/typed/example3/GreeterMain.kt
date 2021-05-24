package akka.kotlin.typed.example3

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.*
import akka.kotlin.typed.onMessage

class GreeterMain private constructor(context: ActorContext<SayHello>) : AbstractBehavior<GreeterMain.SayHello>(context) {

    class SayHello(val name: String)

    private val greeter: ActorRef<Salutation> = context.spawn(Greeter2(), "greeter")
    //private val greeter: ActorRef<Salutation> = context.spawn(Greeter3.create(), "greeter")

    override fun createReceive(): Receive<SayHello> = newReceiveBuilder()
        .onMessage (::onSayHello)
        .build()

    private fun onSayHello(command: SayHello): Behavior<SayHello?> {
        val replyTo = context.spawn(GreeterBot.create(3), command.name)
        greeter.tell(DamnYou(command.name, replyTo))
        return this
    }

    companion object {
        fun create(): Behavior<SayHello> = Behaviors.setup { GreeterMain(it) }
    }
}
