package com.smileshark.ai.service;

import com.smileshark.entity.Session;
import com.smileshark.websocket.endpoint.UserServiceEndpoint;
import com.smileshark.websocket.message.ChatMessage;

public interface AIService {
    void turnToManualJudgment(Session chatSession, ChatMessage message);

    void chat(Session chatSession, ChatMessage message, UserServiceEndpoint userServiceEndpoint);
}
