package com.smileshark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smileshark.common.Result;
import com.smileshark.entity.SessionLog;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

public interface SessionLogService extends IService<SessionLog> {
    void add(String conversationId, List<Message> messages);

    List<SessionLog> tryGetSessionLogs(String sessionId);
    void addToRedis(String sessionId, List<SessionLog> sessionLogs);

    Result<?> readCtMessage(Integer sessionId, Integer userId);

    Result<?> readUserMessage(Integer sessionId, Integer ctId);

    Result<List<SessionLog>> getWindowMessage(Integer sessionId);

    Result<Integer> userGetUnreadMessageCount(Integer sessionId);

    Result<Integer> ctGetUnreadMessageCount(Integer sessionId);
}