package akka.kotlin.shared

import kotlinx.coroutines.*


// Message types for akka.kotlin.standardKotlin.counterActor
sealed class CounterMsg
object IncCounter : CounterMsg() // one-way message to increment counter
class GetCounter(val response: CompletableDeferred<Int>) : CounterMsg() // a request with reply

