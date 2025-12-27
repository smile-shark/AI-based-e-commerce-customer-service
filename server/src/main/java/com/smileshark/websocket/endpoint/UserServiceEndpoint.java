package com.smileshark.websocket.endpoint;

import com.smileshark.ai.service.AIService;
import com.smileshark.entity.SessionLog;
import com.smileshark.mapper.SessionLogMapper;
import com.smileshark.service.SessionService;
import com.smileshark.utils.SessionFind;
import com.smileshark.websocket.message.ChatMessage;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/user/chat/{userId}")
@RequiredArgsConstructor
public class UserServiceEndpoint implements WebSocketEndpoint {
    private static final ConcurrentHashMap<Integer, UserServiceEndpoint> userEndpointPool = new ConcurrentHashMap<>();
    private final SessionLogMapper sessionLogMapper;
    private final SessionFind sessionFind;
    private final SessionService sessionService;
    private final AIService aiService;

    private Session session;
    private Integer userId;

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Integer userId) {
        this.session = session;
        this.userId = userId;
        userEndpointPool.put(userId, this);
    }

    @OnClose
    public void onClose() {
        if (userId != null) {
            userEndpointPool.remove(userId);
        }
    }

    @OnMessage
    @Transactional
    public void onMessage(ChatMessage message, Session session) throws EncodeException, IOException {
        message.setType(getEndpointType());
        // 判断是否有对应的sessionId如果没有就表示是第一次聊天
        com.smileshark.entity.Session chatSession = sessionService.find(message, userId);
        // 将消息存入数据库中
        sessionLogMapper.insert(SessionLog.builder()
                .type(message.getType())
                .sessionId(message.getSessionId())
                .content(message.getMessage())
                .build());
        // 判断session的类型
        switch (chatSession.getConversationStatus()) {
            case AI -> {
                aiService.turnToManualJudgment(chatSession, message);
                if (chatSession.getConversationStatus() == com.smileshark.entity.Session.ConversationStatus.HUMAN) {
                    // 背AI判定为需要转人工
                    CommercialTenantEndpoint ctEndPoint = sessionFind.findCommercialTenantEndPoint(message.getSessionId());
                    if (ctEndPoint != null) {
                        ctEndPoint.sendMessage(message);
                    }
                } else {
                    aiService.chat(chatSession, message, this);
                }
            }
            case HUMAN -> {
                // 通过message中的sessionId找到对应的商户会话
                CommercialTenantEndpoint ctEndPoint = sessionFind.findCommercialTenantEndPoint(message.getSessionId());
                if (ctEndPoint != null) {
                    ctEndPoint.sendMessage(message);
                }
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        try {
            session.getBasicRemote().sendObject(ChatMessage.builder()
                    .state(ChatMessage.State.ERROR)
                    .message(error.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 封装发送消息的方法
     */
    public void sendMessage(ChatMessage chatMessage) throws EncodeException, IOException {
        this.session.getBasicRemote().sendObject(chatMessage);
    }

    @Override
    public SessionLog.Type getEndpointType() {
        return SessionLog.Type.USER;
    }

    public static UserServiceEndpoint findEndPoint(Integer userId) {
        return userEndpointPool.get(userId);
    }
}
