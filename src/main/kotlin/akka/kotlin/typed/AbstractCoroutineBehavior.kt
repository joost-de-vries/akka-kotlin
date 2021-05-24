package akka.kotlin.typed

import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.ReceiveBuilder
import akka.kotlin.classic.coroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class AbstractCoroutineBehavior<E> (context: ActorContext<E>) : AbstractBehavior<E>(context), CoroutineScope {
    private val job = Job()
    private val scope = CoroutineScope(coroutineDispatcher() + job)
    override val coroutineContext: CoroutineContext = scope.coroutineContext

    override fun newReceiveBuilder(): ReceiveBuilder<E> = super.newReceiveBuilder()
        .onSignal(PostStop::class.java,::onPostStop)

    private fun onPostStop(postStop: PostStop): Behavior<E> {
        job.complete()
        return this
    }
}