package com.inconcert.domain.notification.service;

import com.inconcert.domain.notification.entity.Message;
import com.inconcert.domain.notification.repository.MessageRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;

    public List<Message> getMessagesByUserId(Long userId) {
        return messageRepository.findByUserIdOrderByIsReadAscCreatedAtDesc(userId);
    }

    public void markMessageAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
        message.markAsRead();
        messageRepository.save(message);
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    public void saveMessage(String title, String body, User user, Post post) {
        System.out.println("saveMessage 호출");
        com.inconcert.domain.notification.entity.Message message = new com.inconcert.domain.notification.entity.Message(title, body, user, post);
        messageRepository.save(message);
    }
}