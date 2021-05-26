package akka.kotlin.classic

import akka.actor.AbstractActor
import akka.japi.pf.ReceiveBuilder

inline fun <reified A> AbstractActor.match(noinline block: (a: A)-> Unit): ReceiveBuilder = receiveBuilder()
    .match(A::class.java, block)
inline fun <reified A> ReceiveBuilder.match(noinline block: (a: A)-> Unit): ReceiveBuilder = match(A::class.java, block)