package akka.kotlin.classic

import akka.actor.PoisonPill
import akka.event.Logging
import akka.persistence.AbstractPersistentActor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class AbstractCoroutinePersistentActor: AbstractPersistentActor(), CoroutineScope {
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

    override val coroutineContext: CoroutineContext = coroutineDispatcher() + job

    override fun postStop() {
        log.debug("stopping coroutines of ${this.javaClass.simpleName}")
        job.complete()
        super.postStop()
    }
}