package akka.kotlin.typed.example2

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Props
import akka.kotlin.typed.runAkka
import akka.kotlin.typed.ask
import kotlinx.coroutines.delay
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() : Unit = runAkka (GreeterMain.create()) {

    val greeter = actorSystem.greeter()

    val response = greeter.damnYou("charles")

    println("response is $response")
    delay(5000)
}
fun ActorSystem<GreeterMain.SayHello>.greeter() = systemActorOf(Greeter.create(), "greeter", Props.empty())
suspend fun ActorRef<Salutation>.damnYou(name:String): SalutationResponse = ask { DamnYou(name, it) }

