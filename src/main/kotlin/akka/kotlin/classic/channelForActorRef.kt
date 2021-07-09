package akka.kotlin.classic

import akka.actor.ActorRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

@Suppress("EXPERIMENTAL_API_USAGE")
fun <E> CoroutineScope.channelFor(actor: ActorRef): Pair<SendChannel<E>, ReceiveChannel<E>> {
    val toActorChannel = Channel<E>()
    val fromActorChannel = Channel<E>()

    launch {
        while (!toActorChannel.isClosedForReceive) {
            val msg = toActorChannel.receive()
            launch{
                val response: E = actor.ask(msg)
                fromActorChannel.send(response)
            }
        }
    }

    return toActorChannel to fromActorChannel
}
