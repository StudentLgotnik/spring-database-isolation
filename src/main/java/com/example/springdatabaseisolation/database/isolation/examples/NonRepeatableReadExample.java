package com.example.springdatabaseisolation.database.isolation.examples;

import com.example.springdatabaseisolation.database.entity.Message;
import com.example.springdatabaseisolation.database.repository.MessageRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.concurrent.Exchanger;

@Component
public class NonRepeatableReadExample {

    private static final Exchanger<String> exchanger = new Exchanger<>();

    private final MessageRepository messageRepository;

    public NonRepeatableReadExample(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message createMessage(String message) {
        return messageRepository.save(new Message(message));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Exchanger<String> updateMessageWithCommit(String id, String message) {
        exchange("wait update", "read");
        Message readMessage = messageRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Can not find message with id: " + id));
        System.out.println("Read message before update: " + readMessage);

        readMessage.setMessage(message);
        messageRepository.save(readMessage);

        Message updatedMessage = messageRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Can not find message with id: " + id));
        System.out.println("Read message after update: " + updatedMessage);

        return exchanger;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void repeatedReadMessage(String id) {
        Message readMessage = messageRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Can not find message with id: " + id));
        System.out.println("Read message 1: " + readMessage);

        exchange("read", "wait update");
        exchange("read", "commited");

        readMessage = messageRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Can not find message with id: " + id));

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
