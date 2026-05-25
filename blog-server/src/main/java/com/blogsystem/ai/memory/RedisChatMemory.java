package com.blogsystem.ai.memory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;

public class RedisChatMemory implements ChatMemory {

    private final StringRedisTemplate redis;
    private static final String PREFIX = "chat:mem:";
    private static final int MAX_SIZE = 40;

    public RedisChatMemory(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        for (Message msg : messages) {
            String type = msg instanceof UserMessage ? "U:" : "A:";
            redis.opsForList().rightPush(PREFIX + conversationId, type + msg.getText());
        }
        Long size = redis.opsForList().size(PREFIX + conversationId);
        if (size != null && size > MAX_SIZE) {
            redis.opsForList().trim(PREFIX + conversationId, size - MAX_SIZE, -1);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        List<String> raw = redis.opsForList().range(PREFIX + conversationId, 0, -1);
        if (raw == null || raw.isEmpty()) return List.of();
        List<Message> result = new ArrayList<>();
        for (String s : raw) {
            if (s.startsWith("U:")) {
                result.add(new UserMessage(s.substring(2)));
            } else if (s.startsWith("A:")) {
                result.add(new AssistantMessage(s.substring(2)));
            }
        }
        return result;
    }

    @Override
    public void clear(String conversationId) {
        redis.delete(PREFIX + conversationId);
    }
}
