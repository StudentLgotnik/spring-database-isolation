package com.example.springdatabaseisolation.controller;

import com.example.springdatabaseisolation.database.entity.Message;
import com.example.springdatabaseisolation.database.repository.MessageRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/message", produces = "application/json")
public class MessageController {

    private final MessageRepository messageRepository;

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping
    public List<Message> getMessages() {
        return messageRepository.findAll();
    }

    @PostMapping
    public Message createMessage(@RequestBody String message) {
        return messageRepository.save(new Message(message));
    }

}
