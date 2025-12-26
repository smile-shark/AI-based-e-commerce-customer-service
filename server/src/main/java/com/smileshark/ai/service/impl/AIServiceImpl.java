package com.smileshark.ai.service.impl;

import com.smileshark.ai.service.AIService;
import com.smileshark.entity.Session;
import com.smileshark.websocket.endpoint.UserServiceEndpoint;
import com.smileshark.websocket.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    @Override
    public void turnToManualJudgment(Session chatSession, ChatMessage message) {
        // TODO 添加人工判断逻辑
    }

    @Override
    public void chat(Session chatSession, ChatMessage message, UserServiceEndpoint userServiceEndpoint) {
        // TODO RAG检索专业知识回答
    }
}
