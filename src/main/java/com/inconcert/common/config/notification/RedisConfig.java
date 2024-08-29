package com.inconcert.common.config.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                   MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("notification"));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter() {
        return new MessageListenerAdapter((MessageListener) (message, pattern) -> {
            String messageBody = new String(message.getBody());
            String[] parts = messageBody.split(":", 2);
            if (parts.length == 2) {
                Long userId = Long.parseLong(parts[0]);
                String actualMessage = parts[1];
                log.debug("Notification received for user {}: {}", userId, actualMessage);
            } else {
                log.error("Invalid notification format");
            }
        });
    }
}
