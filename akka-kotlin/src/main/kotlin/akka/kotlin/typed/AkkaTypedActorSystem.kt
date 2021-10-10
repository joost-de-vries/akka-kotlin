package akka.kotlin.typed

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.Scheduler
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.AskPattern
import akka.actor.typed.javadsl.Receive
import akka.actor.typed.javadsl.ReceiveBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds
import kotlin.time.toJavaDuration

class AkkaTypedActorSystem<A> @OptIn(ExperimentalTime::class) constructor(
    val actorSystem: ActorSystem<A>,
    val scheduler: Scheduler?=null,
    val timeout: Duration=1.seconds
) : AbstractCoroutineContextElement(AkkaTypedActorSystem) {

    @OptIn(ExperimentalTime::class)
    constructor(guardianBehavior: Behavior<A>, name: String,scheduler:Scheduler?=null,timeout:Duration=1.seconds):
            this(ActorSystem.create(guardianBehavior,name),scheduler,timeout)

    companion object Key : CoroutineContext.Key<AkkaTypedActorSystem<*>>
}

fun <A> CoroutineScope.akkaTypedActorSystem(): AkkaTypedActorSystem<A> = coroutineContext.akkaTypedActorSystem()
fun <A> CoroutineContext.akkaTypedActorSystem(): AkkaTypedActorSystem<A> = this[AkkaTypedActorSystem] as AkkaTypedActorSystem<A>

fun <A, B> runAkka(guardianBehavior: Behavior<A>, block: suspend AkkaScope<A>.()-> B): B =
    runBlocking(AkkaTypedActorSystem(guardianBehavior, "default")) {
        val actorSystem = akkaTypedActorSystem<A>().actorSystem
        try{
            object: AkkaScope<A>, CoroutineScope by this{
                override val actorSystem: ActorSystem<A>
                    get() =actorSystem
            }.block()

        }finally {
            actorSystem.terminate()
        }
    }

//fun <T,E> runAkka(context: CoroutineContext = EmptyCoroutineContext, block: suspend AkkaScope<E>.() -> T): T {
//    val akkaActorSystemFromContext = context[AkkaTypedActorSystem]
//    val (propagateContext, actorSystem) = if(akkaActorSystemFromContext!=null) context to akkaActorSystemFromContext.actorSystem else {
//        val akkaActorSystem = AkkaTypedActorSystem<E>()
//        (context + akkaActorSystem) to akkaActorSystem.actorSystem
//    }
//    val result =  runBlocking(propagateContext) {
//        object : AkkaScope, CoroutineScope by this {
//            override val actorSystem: akka.actor.ActorSystem
//                get() = actorSystem
//        }.block()
//    }
//    actorSystem.terminate()
//    return result
//}

interface AkkaScope<E> : CoroutineScope {
    val actorSystem: ActorSystem<E>
}
inline fun <reified M:T,T> ReceiveBuilder<T>.onMessageSuspend(noinline f: suspend (M)-> Behavior<T>): ReceiveBuilder<T>{
    val g: (M)-> Behavior<T> = {runBlocking { f(it) } } // this blocks the akka thread?
       return this.onMessage(M::class.java, g)
}


inline fun <reified M:T,T> ReceiveBuilder<T>.onMessage(noinline f: (M)-> Behavior<T>): ReceiveBuilder<T> =
    this.onMessage(M::class.java,f)
inline fun <reified M:T,T> AbstractBehavior<T>.onMessage(noinline f: (M)-> Behavior<T>): Receive<T> =
    newReceiveBuilder().onMessage(f).build()

@OptIn(ExperimentalTime::class)
suspend fun <Request,Response> ActorRef<Request>.ask(timeout: Duration? = null, scheduler: Scheduler? = null, f:(ActorRef<Response>) -> Request): Response {
    val actorSystem = coroutineContext.akkaTypedActorSystem<Any>()
    return AskPattern.ask(this,f, (timeout?:actorSystem.timeout).toJavaDuration(), scheduler?:actorSystem.scheduler?:actorSystem.actorSystem.scheduler()).await()
}
