package com.smileshark.ai.memory;

import com.smileshark.mapper.SessionLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomizationMemory implements ChatMemory {
    private final StringRedisTemplate stringRedisTemplate;
    private final SessionLogMapper sessionLogMapper;
    @Value("${memory.redis-length}")
    private Integer memoryLength;
    // TODO 完成自定义记忆
    @Override
    public void add(String conversationId, List<Message> messages) {

    }

    @Override
    public List<Message> get(String conversationId) {
        return List.of();
    }

    @Override
    public void clear(String conversationId) {

    }
}
