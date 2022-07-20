package com.example.springdatabaseisolation.database.isolation.examples;

import com.example.springdatabaseisolation.database.entity.Message;
import com.example.springdatabaseisolation.database.repository.MessageRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.concurrent.Exchanger;

@Component
public class DirtyReadExample {

    private static final Exchanger<String> exchanger = new Exchanger<>();

    private final MessageRepository messageRepository;

    public DirtyReadExample(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message createMessage(String message) {
        return messageRepository.save(new Message(message));
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void updateMessageWithRollback(String id, String message) {
        Message readMessage = messageRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Can not find message with id: " + id));
        System.out.println("Read message before update: " + readMessage);

        readMessage.setMessage(message);
        messageRepository.save(readMessage);

        Message updatedMessage = messageRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Can not find message with id: " + id));
        System.out.println("Read message after update: " + updatedMessage);

        exchange("updated", "not read");
        exchange("updated", "read");

        System.out.println("Rollback!");
        throw new RuntimeException("Rollback");
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void readDirtyMessage(String id) {
        exchange("not read", "updated");
        Message readMessage = messageRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Can not find message with id: " + id));
        exchange("read", "updated");

        System.out.println("Read message: " + readMessage);
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
