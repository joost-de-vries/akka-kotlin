package akka.kotlin.classic

import akka.actor.ActorRef
import akka.pattern.Patterns
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.future.await
import scala.compat.java8.FutureConverters.toJava
import scala.concurrent.Future

suspend fun <E> Future<E>.await() = toJava(this).await()

fun ActorRef.tell(msg: Any){ tell(msg, ActorRef.noSender())}

@Suppress("unchecked_cast")
suspend fun <Request,Response>  ActorRef.ask(request: Request, timeoutMillis: Long = 1000L): Response {
    val response = toJava(Patterns.ask(this,request,timeoutMillis)).await()
    return response as Response
}
fun <Request,Response>  ActorRef.askAsync(request: Request, timeoutMillis: Long = 1000L): Deferred<Response> {
    val response = toJava(Patterns.ask(this,request,timeoutMillis)).asDeferred()
    return response as Deferred<Response>
}
