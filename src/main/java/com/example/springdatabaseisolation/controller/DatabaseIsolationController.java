package com.example.springdatabaseisolation.controller;

import com.example.springdatabaseisolation.database.entity.Message;
import com.example.springdatabaseisolation.database.isolation.examples.DirtyReadExample;
import com.example.springdatabaseisolation.database.isolation.examples.NonRepeatableReadExample;
import com.example.springdatabaseisolation.database.isolation.examples.PhantomReadExample;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/database/isolation")
public class DatabaseIsolationController {

    private final DirtyReadExample dirtyReadExample;
    private final NonRepeatableReadExample nonRepeatableReadExample;
    private final PhantomReadExample phantomReadExample;

    public DatabaseIsolationController(DirtyReadExample dirtyReadExample, NonRepeatableReadExample nonRepeatableReadExample, PhantomReadExample phantomReadExample) {
        this.dirtyReadExample = dirtyReadExample;
        this.nonRepeatableReadExample = nonRepeatableReadExample;
        this.phantomReadExample = phantomReadExample;
    }

    //http://repository.transtep.com/repository/thirdparty/H2/1.0.63/docs/html/advanced.html
    //https://vladmihalcea.com/dirty-read/
    @GetMapping("/dirty-read-example")
    public void dirtyReadExample() {
        Message message = dirtyReadExample.createMessage("Dirty read message!");
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> dirtyReadExample.updateMessageWithRollback(message.getId(), "Dirty read message updated!"));
        executorService.execute(() -> dirtyReadExample.readDirtyMessage(message.getId()));
        System.out.println("Done!");
        executorService.shutdown();
    }

    //https://vladmihalcea.com/non-repeatable-read/
    @GetMapping("/non-repeatable-read-example")
    public void nonRepeatableReadExample() throws ExecutionException, InterruptedException {
        Message message = nonRepeatableReadExample.createMessage("Non repeatable read message!");
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<Exchanger<String>> exchangerFuture = executorService.submit(() -> nonRepeatableReadExample.updateMessageWithCommit(message.getId(), "Non repeatable read message updated!"));
        executorService.execute(() -> nonRepeatableReadExample.repeatedReadMessage(message.getId()));
        exchangerFuture.get().exchange("commited");
        System.out.println("Commit!");
        System.out.println("Done!");
        executorService.shutdown();
    }

    //https://vladmihalcea.com/phantom-read/
    @GetMapping("/phantom-read-example")
    public void phantomReadExample() throws ExecutionException, InterruptedException {
        List<Message> message = phantomReadExample.createMessages("Phantom read message!");
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<Exchanger<String>> exchangerFuture = executorService.submit(() -> phantomReadExample.addMessageWithCommit("Phantom read message with commit!"));
        executorService.execute(() -> phantomReadExample.phantomReadMessages("Phantom read message"));
        exchangerFuture.get().exchange("commited");
        System.out.println("Commit!");
        System.out.println("Done!");
        executorService.shutdown();
    }
}
