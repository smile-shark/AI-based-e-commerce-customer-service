package com.smileshark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smileshark.entity.SessionLog;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

public interface SessionLogService extends IService<SessionLog> {
    void add(String conversationId, List<Message> messages);

    List<SessionLog> tryGetSessionLogs(String sessionId);
    void addToRedis(String sessionId, List<SessionLog> sessionLogs);
}