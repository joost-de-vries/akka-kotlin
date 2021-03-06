### kotlin coroutines and akka actors

These are some experiments in what suspendable functions and channels can mean for Akka.  
I wrote [an article](https://gist.github.com/joost-de-vries/479bbd285c4de23cb5bade21d83e2e22) about it.  

- [use channels](akka-kotlin/src/test/kotlin/akka/kotlin/classic/example0/channels.kt) to send messages to an Akka actor and receive them.  
- [a channel as inbox](akka-kotlin/src/test/kotlin/akka/kotlin/classic/example4/channelInbox.kt) in the actor implementation. Calling async methods as suspending functions.  
- a channel as inbox in the actor implementation [with exception handling](akka-kotlin/src/test/kotlin/akka/kotlin/classic/example5/channelInboxWithErrorHandling.kt).


Also in there: 
- use suspendable functions to call (ask) akka actor.
- use functions with receiver for an imperative seeming api.  
- use extension functions to document the request response protocol for the actor.    
- make actorsystem available in coroutine context.
- make akka actor be the coroutine context so it can call suspendable functions. The coroutines involved have the lifecycle of the actor.



Kotlin actors send a CompletableDeferred in a message to an actor. We [can do this for Akka](akka-kotlin/src/test/kotlin/akka/kotlin/classic/local/completableDeferred.kt). But that won't work across a cluster of course. So it's of limited use.



