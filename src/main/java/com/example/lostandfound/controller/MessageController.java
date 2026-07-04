package com.example.lostandfound.controller;

import com.example.lostandfound.dto.message.MessageRequest;
import com.example.lostandfound.dto.message.MessageResponse;
import com.example.lostandfound.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/message")
    public ResponseEntity<MessageResponse> send(@Valid @RequestBody MessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.send(request));
    }

    @GetMapping("/messages/{userId}")
    public List<MessageResponse> getForUser(@PathVariable Long userId) {
        return messageService.getForUser(userId);
    }
}
