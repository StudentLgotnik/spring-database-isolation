package com.example.springdatabaseisolation.database.isolation.examples;

import com.example.springdatabaseisolation.database.entity.Message;
import com.example.springdatabaseisolation.database.repository.MessageRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.Exchanger;

@Component
public class PhantomReadExample {

    private static final Exchanger<String> exchanger = new Exchanger<>();

    private final MessageRepository messageRepository;

    public PhantomReadExample(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public List<Message> createMessages(String message) {
        return messageRepository.saveAll(List.of(
                new Message(message + 1),
                new Message(message + 2),
                new Message(message + 3)
        ));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Exchanger<String> addMessageWithCommit(String message) {
        exchange("wait add", "read");

        messageRepository.save(new Message(message));

        return exchanger;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void phantomReadMessages(String messagePart) {
        List<Message> readMessage = messageRepository.findAllByMessageContains(messagePart);
        System.out.println("Read message 1: " + readMessage);

        exchange("read", "wait add");
        exchange("read", "commited");

        readMessage = messageRepository.findAllByMessageContains(messagePart);

        System.out.println("Read message 2: " + readMessage);
    }

    private void exchange(String toSend, String receive) {
        try {
            String exchMsg = exchanger.exchange(toSend);
            assert receive.equals(exchMsg): String.format("Not %s!", receive);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
