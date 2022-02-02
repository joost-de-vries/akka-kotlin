package akka.kotlin.classic

import akka.actor.AbstractActor
import akka.actor.AbstractActorWithStash
import akka.actor.PoisonPill
import akka.event.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class AbstractCoroutineActor() : AbstractActor(), CoroutineScope {
    private val log = Logging.getLogger(context.system, this)

    private val job = Job()

    init {
        job.invokeOnCompletion {
            if (it != null) {
                log.error(it, "Unhandled exception in coroutine")
                self.tell(PoisonPill.getInstance(), null)
            }
        }
    }

    private val scope = CoroutineScope(coroutineDispatcher() + job)
    override val coroutineContext: CoroutineContext = scope.coroutineContext

    override fun postStop() {
        System.err.println("stopping ${this.javaClass.simpleName} and coroutine")
        job.complete()
        super.postStop()
    }
}

abstract class AbstractCoroutineActorWithStash() : AbstractActorWithStash(), CoroutineScope {
    private val log = Logging.getLogger(context.system, this)

    private val job = Job()

    init {
        job.invokeOnCompletion {
            if (it != null) {
                log.error(it, "Unhandled exception in coroutine")
                self.tell(PoisonPill.getInstance(), null)
            }
        }
    }

    private val scope = CoroutineScope(coroutineDispatcher() + job)
    override val coroutineContext: CoroutineContext = scope.coroutineContext

    override fun postStop() {
        System.err.println("stopping ${this.javaClass.simpleName} and coroutine")
        job.complete()
        super.postStop()
    }
}
