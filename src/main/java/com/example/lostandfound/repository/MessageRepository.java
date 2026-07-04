package com.example.lostandfound.repository;

import com.example.lostandfound.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Inbox + sent items for a user, newest first
    List<Message> findBySenderIdOrReceiverIdOrderByCreatedAtDesc(Long senderId, Long receiverId);

    List<Message> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
}
