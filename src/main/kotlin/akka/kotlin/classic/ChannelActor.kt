package akka.kotlin.classic

import akka.actor.ActorRef
import akka.event.Logging
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ValueOrClosed
import kotlinx.coroutines.selects.SelectClause1

data class ChannelActorMessage<E>(val self: ActorRef, val msg: E, val sender: ActorRef)
object Next

abstract class ChannelActor<E> : AbstractCoroutineActorWithStash() {
    private val log = Logging.getLogger(context.system, this)
    private val _channel = Channel<ChannelActorMessage<E>>()
    val actorChannel = ActorChannel(this.channel)

    val channel: ReceiveChannel<ChannelActorMessage<E>>
        get() = _channel

    private val stashing: Receive = receiveBuilder()
        .match(Next::class.java) {
            unstashAll()
            log("start processing")
            context.become(processing)
        }
        .matchAny { stash() }
        .build()

    private val processing: Receive = receiveBuilder()
        .matchAny {
            log("received msg from inbox, putting into channel. sender is ${sender}")
            val channelMessage = ChannelActorMessage(self = self, msg = it as E, sender = sender)
            context.become(stashing)
            sendToChannel(channelMessage)
        }.build()

    @Suppress("unchecked_cast")
    override fun createReceive(): Receive {
        log("start processing")
        return processing
    }

    private fun sendToChannel(msg: ChannelActorMessage<E>) = runBlocking {
        log("sending channel message ${msg}")
        _channel.send(msg)
        msg.self.tell(Next, null)
    }

    private fun log(msg: String) =
        log.info("${self().path()}: $msg")

    protected fun receive(block: suspend ActorScope<E>.() -> Unit): Unit {
        startReceive(block)
    }
}

fun <E> ChannelActor<E>.startReceive(block: suspend ActorScope<E>.() -> Unit): Unit {
    val scope = object : ActorScope<E>, CoroutineScope by this {
        override val inbox: ReceiveChannel<E>
            get() = actorChannel
//        override val self: ActorRef
//            get() = actorChannel.self
        override val sender: ActorRef
            get() = actorChannel.sender
    }
    launch {
        scope.block()
    }
}

interface ActorScope<E> : CoroutineScope {
    val inbox: ReceiveChannel<E>

    //val self: ActorRef

    val sender: ActorRef
}

class ActorChannel<E>(private val channel: ReceiveChannel<ChannelActorMessage<E>>) : ReceiveChannel<E> {
    lateinit var sender: ActorRef
    lateinit var self: ActorRef

    @ExperimentalCoroutinesApi
    override val isClosedForReceive: Boolean
        get() = channel.isClosedForReceive

    @ExperimentalCoroutinesApi
    override val isEmpty: Boolean
        get() = channel.isEmpty
    override val onReceive: SelectClause1<E>
        get() = TODO("Not yet implemented")

    @InternalCoroutinesApi
    override val onReceiveOrClosed: SelectClause1<ValueOrClosed<E>>
        get() = TODO("Not yet implemented")

    @ObsoleteCoroutinesApi
    override val onReceiveOrNull: SelectClause1<E?>
        get() = TODO("Not yet implemented")

    override fun cancel(cause: Throwable?): Boolean {
        TODO("Not yet implemented")
    }

    override fun cancel(cause: CancellationException?) {
        channel.cancel(cause)
    }

    override fun iterator(): ChannelIterator<E> {
        val channelIterator = channel.iterator()
        return object : ChannelIterator<E> {
            override suspend fun hasNext(): Boolean = channelIterator.hasNext()

            override fun next(): E {
                val msg = channelIterator.next()
                sender = msg.sender
                self = msg.self
                return msg.msg
            }
        }
    }

    override fun poll(): E? = channel.poll()?.msg

    override suspend fun receive(): E {
        val msg = channel.receive()
        self = msg.self
        sender = msg.sender
        return msg.msg
    }

    @InternalCoroutinesApi
    override suspend fun receiveOrClosed(): ValueOrClosed<E> {
        TODO("Not yet implemented")
    }

    @ObsoleteCoroutinesApi
    override suspend fun receiveOrNull(): E? {
        TODO("Not yet implemented")
    }

}

abstract class ChannelActorSuspend<E>() : ChannelActor<E>() {
    private val log = Logging.getLogger(context.system, this)

    init {
        launch {
            while (!channel.isClosedForReceive) {
                val msg = channel.receive()
                receiveSuspend(msg)
            }
        }
    }

    private fun log(msg: String) =
        log.info("${self.path()}: $msg")

    abstract suspend fun receiveSuspend(msg: ChannelActorMessage<E>): Unit
}

