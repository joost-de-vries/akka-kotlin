package akka.kotlin.typed.example3

import akka.actor.typed.ActorSystem
import akka.kotlin.typed.runAkka
import kotlinx.coroutines.delay

fun main() : Unit = runAkka(GreeterMain.create()){

    actorSystem.sayHello("Charles")

    delay(2000)
}
fun ActorSystem<GreeterMain.SayHello>.sayHello(name: String) = tell(GreeterMain.SayHello(name))
