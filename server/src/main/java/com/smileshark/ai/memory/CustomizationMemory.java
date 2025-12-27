package com.smileshark.ai.memory;

import cn.hutool.json.JSONUtil;
import com.smileshark.entity.SessionLog;
import com.smileshark.executor.GlobalTreadPool;
import com.smileshark.service.SessionLogService;
import com.smileshark.utils.KeyUtils;
import com.smileshark.utils.TypeConversion;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomizationMemory implements ChatMemory {
    private final StringRedisTemplate stringRedisTemplate;
    private final SessionLogService sessionLogService;
    @Value("${memory.redis-length}")
    private Integer memoryLength;
    @Value("${memory.expiration-duration}")
    private Integer timeout;
    @Value("${memory.key}")
    private String memoryKey;

    // TODO 完成自定义记忆
    @Override
    public void add(@NotNull String conversationId, @NotNull List<Message> messages) {
        // 使用线程池完成对应记忆的添加
        GlobalTreadPool.executor.execute(() -> sessionLogService.add(conversationId, messages));
    }

    @NotNull
    @Override
    public List<Message> get(@NotNull String conversationId) {
        // 从redis中获取到对应的记忆
        List<String> jsonList = stringRedisTemplate.opsForList().range(KeyUtils.redisKeyUtils(memoryKey, conversationId), -memoryLength, -1);
        if (jsonList == null || jsonList.isEmpty()) {
            // 如果没有这个记忆，就通过mysql进行一次查询
            List<SessionLog> sessionLogs = sessionLogService.tryGetSessionLogs(conversationId);
            // 将查询到的列表添加到redis中
            GlobalTreadPool.executor.execute(() -> sessionLogService.addToRedis(conversationId, sessionLogs));
            // 转换为messages并返回
            List<Message> messages = new ArrayList<>(sessionLogs.size());
            for (SessionLog sessionLog : sessionLogs) {
                Message message = TypeConversion.sessionToMessage(sessionLog.getType(), sessionLog.getContent());
                messages.add(message);
            }
            return messages;
        }
        // 将json转为sessionLog再转为message
        List<Message> messages = new ArrayList<>(jsonList.size());
        for (String json : jsonList) {
            SessionLog sessionLog = JSONUtil.toBean(json, SessionLog.class);
            Message message = TypeConversion.sessionToMessage(sessionLog.getType(), sessionLog.getContent());
            messages.add(message);
        }
        return messages;
    }

    @Override
    public void clear(@NotNull String conversationId) {
        System.out.println("触发清除：" + conversationId);
    }
}
