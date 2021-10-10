package akka.kotlin.classic.persistence.example1

import akka.Done
import akka.actor.ActorRef;
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.kotlin.classic.*
import akka.kotlin.classic.example4.DemoKotlinActor
import akka.pattern.ReplyWith
import akka.pattern.StatusReply
import akka.persistence.AbstractPersistentActor;
import akka.persistence.SnapshotOffer;
import akka.persistence.testkit.javadsl.EventSourcedBehaviorTestKit
import kotlinx.coroutines.delay
import java.time.LocalDateTime

// see https://doc.akka.io/docs/akka/current/persistence.html
data class Cmd(val data: String)
data class Evt(val data: String)
object GetState

data class ExampleState(val events: List<String> = emptyList()) {
    fun with(evt: Evt) = copy(events = events + evt.data)
}

class PersistentKotlinActor(private val id: String) : AbstractCoroutinePersistentActor() {
    private var state = ExampleState()
    private val snapShotInterval = 1000

    override fun createReceive(): Receive =
        match<Cmd> {
            val evt = Evt("${it.data}-${state.events.size}")
            persist(evt) {
                state = state.with(evt)
                context.system.eventStream.publish(it)
                sender.tell(StatusReply.ack(), null)
                if (lastSequenceNr() % snapShotInterval == 0L && lastSequenceNr() != 0L)
                    saveSnapshot(state);
            }
        }
            .match<GetState> {
                sender.tell(StatusReply.success(state.events.size))
            }
            .build()

    override fun persistenceId(): String = id

    override fun createReceiveRecover(): Receive =
        match<Evt> { state = state.with(it) }
            .match<SnapshotOffer> { state = it.snapshot() as ExampleState }
            .build()
}

fun ActorRefFactory.persistentActor(id: String) = actorOf(createProps { PersistentKotlinActor(id) })

suspend fun ActorRefFactory.sendCommand(toId: String, cmd: Cmd): Done =
    persistentActor(toId).ask(cmd)

suspend fun ActorRefFactory.getCount(toId: String): Int =
    persistentActor(toId).ask<GetState, StatusReply<Int>>(GetState).value

fun main() = runAkka(inMemoryPersistence()) {

    val entityId = "1"

    actorSystem.sendCommand(entityId, createCmd())
    actorSystem.sendCommand(entityId, createCmd())
    actorSystem.sendCommand(entityId, createCmd())
    val events = actorSystem.getCount(entityId)
    println("Events: $events")

    delay(3000)
}

fun createCmd() = Cmd(LocalDateTime.now().toString())

fun inMemoryPersistence(name: String = "default") =
    AkkaActorSystem(ActorSystem.create(name, EventSourcedBehaviorTestKit.config()))
