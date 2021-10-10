package akka.kotlin.classic.example_java;

import akka.actor.AbstractActorWithStash;
import akka.kotlin.classic.exampleshared.GetCounter;

import static akka.pattern.Patterns.pipe;

import java.util.concurrent.CompletableFuture;

public class DemoKotlinActor extends AbstractActorWithStash {
    private Integer counter = 0;

//    private Receive processing = receiveBuilder().match(GetCounter.class, (msg ->{
//        counter++;
//        pipe(SlowService.callService(msg.getSeqNr()), context().dispatcher()).to(self());
//context().become(waiting);
//
//    } )).build();
//    private Receive waiting = receiveBuilder()
//            .match(GetCounter.class,msg ->
//        stash())
//            .match(Integer.class, (msg -> )).build();
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(GetCounter.class, (msg ->{
            counter++;
            pipe(SlowService.callService(msg.getSeqNr()), context().dispatcher()).to(self());


        } ))
                .build();
    }
}

class SlowService{
    static CompletableFuture<Integer> callService(Integer i){
        try {
            Thread.sleep(100);
            return CompletableFuture.completedFuture(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}