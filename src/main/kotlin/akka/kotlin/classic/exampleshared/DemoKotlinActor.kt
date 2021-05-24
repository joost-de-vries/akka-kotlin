package akka.kotlin.classic.exampleshared

import akka.actor.ActorRef
import akka.kotlin.classic.ask
import akka.kotlin.classic.askAsync
import kotlinx.coroutines.Deferred


sealed class CounterMsg1{
    abstract val seqNr: Int
}
data class GetCounter(override val seqNr: Int) : CounterMsg1() // one-way message to increment counter
data class CounterResponse(override val seqNr: Int, val counter: Int) : CounterMsg1() // a request with reply

fun ActorRef.getCounterAsync(seqNr: Int, timeout: Long = 1000L): Deferred<CounterResponse> = askAsync(GetCounter(seqNr), timeout)

suspend fun ActorRef.getCounter(seqNr: Int, timeout: Long = 1000L): CounterResponse =
    ask(GetCounter(seqNr), timeout)

fun ActorRef.counterResponse(seqNr: Int, counter: Int) =
    tell(CounterResponse(seqNr=seqNr, counter=counter), null)
