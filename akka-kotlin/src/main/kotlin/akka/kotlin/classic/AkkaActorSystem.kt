package akka.kotlin.classic

import akka.actor.*
import akka.actor.typed.javadsl.*
import kotlinx.coroutines.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class AkkaActorSystem(val actorSystem: ActorSystem) : AbstractCoroutineContextElement(AkkaActorSystem) {
    constructor(): this(ActorSystem.create())
    constructor(name:String): this(ActorSystem.create(name))
    companion object Key : CoroutineContext.Key<AkkaActorSystem>
}

fun CoroutineScope.actorSystem() = (coroutineContext[AkkaActorSystem]?: throw IllegalStateException("Expected AkkaActorSystem to be available in the coroutine context "))
    .actorSystem
fun CoroutineScope.actorSystemOrNull() = coroutineContext[AkkaActorSystem]?.actorSystem

object Props{
    inline fun <reified A: Actor> create(noinline creator: ()->A): akka.actor.Props = akka.actor.Props.create(A::class.java, creator)
}

fun <T> runAkka(context: CoroutineContext = EmptyCoroutineContext, block: suspend AkkaScope.() -> T): T {
    val akkaActorSystemFromContext = context[AkkaActorSystem]
    val (propagateContext, actorSystem) = if(akkaActorSystemFromContext!=null) context to akkaActorSystemFromContext.actorSystem else {
        val akkaActorSystem = AkkaActorSystem()
        (context + akkaActorSystem) to akkaActorSystem.actorSystem
    }
    return try {
        runBlocking(propagateContext) {
            object : AkkaScope, CoroutineScope by this {
                override val actorSystem: ActorSystem
                    get() = actorSystem
            }.block()
        }
    } finally {
        actorSystem.terminate()
    }
}

interface AkkaScope : CoroutineScope {
    val actorSystem: ActorSystem
}

fun AbstractActor.coroutineDispatcher() = this.context.dispatcher.asCoroutineDispatcher()
fun AbstractBehavior<*>.coroutineDispatcher() = this.context.executionContext.asCoroutineDispatcher()

fun ActorRef.respondFailure(t:Throwable) =
    tell(Status.Failure(t), null)
