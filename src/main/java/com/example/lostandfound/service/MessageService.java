package com.example.lostandfound.service;

import com.example.lostandfound.dto.message.MessageRequest;
import com.example.lostandfound.dto.message.MessageResponse;
import com.example.lostandfound.entity.Message;
import com.example.lostandfound.entity.User;
import com.example.lostandfound.exception.BadRequestException;
import com.example.lostandfound.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserService userService;

    public MessageResponse send(MessageRequest request) {
        if (request.getSenderId().equals(request.getReceiverId())) {
            throw new BadRequestException("You cannot send a message to yourself");
        }

        User sender = userService.findUserOrThrow(request.getSenderId());
        User receiver = userService.findUserOrThrow(request.getReceiverId());

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .message(request.getMessage())
                .build();

        return MessageResponse.from(messageRepository.save(message));
    }

    /**
     * All messages (sent + received) for a user, newest first -
     * powers the "Messages" tab on the dashboard.
     */
    public List<MessageResponse> getForUser(Long userId) {
        userService.findUserOrThrow(userId); // 404 if the user doesn't exist
        return messageRepository.findBySenderIdOrReceiverIdOrderByCreatedAtDesc(userId, userId).stream()
                .map(MessageResponse::from)
                .toList();
    }
}
