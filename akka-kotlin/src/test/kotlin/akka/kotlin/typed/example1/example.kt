package akka.kotlin.typed.example1

import akka.actor.typed.ActorSystem
import akka.kotlin.typed.example1.GreeterMain.SayHello

fun main() {
    val greeterMain = ActorSystem.create(GreeterMain.create(), "helloakka")

    greeterMain.tell(SayHello("Charles"))

    try {
        println(">>> Press ENTER to exit <<<")
        System.`in`.read()
    } finally {
        greeterMain.terminate()
    }
}
