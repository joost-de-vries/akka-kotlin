package akka.kotlin.typed

import akka.actor.typed.Behavior
import akka.actor.typed.internal.ActorContextImpl
import akka.actor.typed.internal.adapter.ActorContextAdapter
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AkkaTypedChannel<E> private constructor(
    context: ActorContext<E>,
    val bh: suspend ActorWithChannel<E>.() -> Unit
) : AbstractCoroutineBehavior<E>(context) {

    val channel = Channel<E>()

    init {
        launch { actorWithChannel(channel, context).bh() }
    }

    override fun createReceive(): Receive<E> = newReceiveBuilder()
        .onAnyMessage(::onGreet)
        .build()

    private fun onGreet(command: E): Behavior<E> {
        runBlocking { channel.send(command) }
        return this
    }

    companion object {
        fun <E> create(bh: suspend ActorWithChannel<E>.() -> Unit): Behavior<E> =
            Behaviors.setup { AkkaTypedChannel(it, bh) }
    }
}

interface ActorWithChannel<E> {
    val channel: ReceiveChannel<E>
    val context: ActorContext<E>
}

fun <E> actorWithChannel(ch: Channel<E>, actorContext: ActorContext<E>) = object : ActorWithChannel<E> {
    override val channel: ReceiveChannel<E> = ch
    override val context: ActorContext<E> = KotlinActorContext(actorContext as ActorContextAdapter<E>)
}

class KotlinActorContext<E>(
    proxy: ActorContextAdapter<E>) : ActorContextImpl<E> by proxy{
    override fun checkCurrentActorThread() {
        //super.checkCurrentActorThread()
    }
}