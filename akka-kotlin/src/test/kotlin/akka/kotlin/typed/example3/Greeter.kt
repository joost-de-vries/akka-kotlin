package akka.kotlin.typed.example3

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import akka.kotlin.typed.*
import kotlinx.coroutines.delay

sealed class Salutation()
data class Greet(val whom: String, val replyTo: ActorRef<SalutationResponse>) : Salutation()
data class DamnYou(val whom: String, val replyTo: ActorRef<SalutationResponse>) : Salutation()

sealed class SalutationResponse()
data class Greeted(val whom: String, val from: ActorRef<Salutation>) : SalutationResponse()
data class DamnYouToo(val whom: String, val from: ActorRef<Salutation>) : SalutationResponse()

class Greeter private constructor(context: ActorContext<Salutation>) : AbstractBehavior<Salutation>(context) {

    override fun createReceive(): Receive<Salutation> = newReceiveBuilder()
        .onMessage(::onGreet)
        .onMessage(::onDamnYou)
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

fun Greeter2(): Behavior<Salutation> = AkkaTypedChannel.create {
    for (command in channel) {
        when (command) {
            is Greet -> {
                //context.log.info("Hello {}!", command.whom)
                context.log.info("Hello {}!", command.whom)
                command.replyTo.tell(Greeted(command.whom, context.self))
            }
            is DamnYou -> {
                //context.log.info("Damn you {}!", command.whom)  // <- doesn't work because of 1)
                context.log.info("Damn you {}!", command.whom)
                command.replyTo.tell(DamnYouToo(command.whom, context.self))
            }
        }
    }

}

/* 1)
java.lang.UnsupportedOperationException: Unsupported access to ActorContext from the outside of Actor[akka://helloakka/user/greeter#-1115937915]. No message is currently processed by the actor, but ActorContext was called from Thread[helloakka-akka.actor.default-dispatcher-3,5,main].
	at akka.actor.typed.internal.ActorContextImpl.checkCurrentActorThread(ActorContextImpl.scala:347)
	at akka.actor.typed.internal.ActorContextImpl.checkCurrentActorThread$(ActorContextImpl.scala:335)
	at akka.actor.typed.internal.adapter.ActorContextAdapter.checkCurrentActorThread(ActorContextAdapter.scala:49)
	at akka.actor.typed.internal.ActorContextImpl.log(ActorContextImpl.scala:163)
	at akka.actor.typed.internal.ActorContextImpl.log$(ActorContextImpl.scala:162)
	at akka.actor.typed.internal.adapter.ActorContextAdapter.log(ActorContextAdapter.scala:49)
	at akka.actor.typed.internal.ActorContextImpl.getLog(ActorContextImpl.scala:169)
	at akka.actor.typed.internal.ActorContextImpl.getLog$(ActorContextImpl.scala:169)
	at akka.actor.typed.internal.adapter.ActorContextAdapter.getLog(ActorContextAdapter.scala:49)
	at akka.kotlin.typedChannel.GreeterKt$Greeter2$1.invokeSuspend(Greeter.kt:82)
	at akka.kotlin.typedChannel.GreeterKt$Greeter2$1.invoke(Greeter.kt)
	at AkkaTypedChannel$1.invokeSuspend(Greeter.kt:58)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
 */

//class Greeter2 private constructor(context: ActorContext<Salutation>, val bh: suspend AkkaChannel<Salutation>.()->Unit) : AbstractCoroutineBehavior<Salutation>(context) {
//
//    val channel = Channel<Salutation>()
//    init {
//        launch { AkkaChannel(channel).bh() }
//    }
//    override fun createReceive(): Receive<Salutation> = newReceiveBuilder()
//        .onMessage(::onGreet)
//        .build()
//
//    private fun onGreet(command: Salutation): Behavior<Salutation> {
//        launch { channel.send(command) }
//        return this
//    }
//}

//class KotlinActorContext<E>(akkaActorContext: ActorContext<E>) : ActorContext<E> by akkaActorContext{
//
//}

object Customer


suspend fun findCustomer(customerId:String): Customer {
    delay(1)
    return Customer
}

class Greeter3 private constructor(context: ActorContext<Salutation>) : AbstractCoroutineBehavior<Salutation>(context) {

    override fun createReceive(): Receive<Salutation> =
        onMessage(::onSalutation)

    private fun onSalutation(command: Salutation): Behavior<Salutation> = when (command) {
        is Greet -> {
            context.log.info("Hello {}!", command.whom)
            command.replyTo.tell(Greeted(command.whom, context.self))
            this
        }
        is DamnYou -> {
            context.log.info("Damn you {}!", command.whom)
            command.replyTo.tell(DamnYouToo(command.whom, context.self))
            this
        }
    }

    companion object {
        fun create(): Behavior<Salutation> = Behaviors.setup { Greeter3(it) }
    }
}

class Greeter4 private constructor(context: ActorContext<Salutation>) : AbstractBehavior<Salutation>(context) {

    override fun createReceive(): Receive<Salutation> = onMessage { command ->
        when (command ) {
            is Greet -> {
                context.log.info("Hello {}!", command.whom)
                command.replyTo.tell(Greeted(command.whom, context.self))
                this
            }
            is DamnYou -> {
                context.log.info("Damn you {}!", command.whom)
                command.replyTo.tell(DamnYouToo(command.whom, context.self))
                this
            }
        }
    }

    companion object {
        fun create(): Behavior<Salutation> = Behaviors.setup { Greeter4(it) }
    }
}
