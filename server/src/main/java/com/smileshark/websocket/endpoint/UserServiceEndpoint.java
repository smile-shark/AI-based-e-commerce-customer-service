package com.smileshark.websocket.endpoint;

import com.smileshark.ai.service.AIService;
import com.smileshark.config.ChatMessageCoder;
import com.smileshark.entity.SessionLog;
import com.smileshark.mapper.SessionLogMapper;
import com.smileshark.service.SessionService;
import com.smileshark.utils.SessionFind;
import com.smileshark.websocket.message.ChatMessage;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/user/chat/{userId}", decoders = ChatMessageCoder.class, encoders = ChatMessageCoder.class)
public class UserServiceEndpoint implements WebSocketEndpoint {
    private static final ConcurrentHashMap<Integer, UserServiceEndpoint> userEndpointPool = new ConcurrentHashMap<>();

    // --- 1. 所有的 Service 必须声明为 static ---
    private static SessionLogMapper sessionLogMapper;
    private static SessionFind sessionFind;
    private static SessionService sessionService;
    private static AIService aiService;

    // --- 2. 通过非静态 Setter 方法进行注入 ---
    // Spring 启动时会创建一个该类的单例，并调用此方法给静态变量赋值
    @Autowired
    public void setDependencies(SessionLogMapper sessionLogMapper, SessionFind sessionFind,
                                SessionService sessionService, AIService aiService) {
        UserServiceEndpoint.sessionLogMapper = sessionLogMapper;
        UserServiceEndpoint.sessionFind = sessionFind;
        UserServiceEndpoint.sessionService = sessionService;
        UserServiceEndpoint.aiService = aiService;
    }

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
    public void onMessage(ChatMessage message, Session session) throws EncodeException, IOException, IllegalAccessException {
        message.setType(getEndpointType());
        // 判断是否有对应的sessionId如果没有就表示是第一次聊天
        com.smileshark.entity.Session chatSession = sessionService.find(message, userId, this);
        // 判断session的类型
        switch (chatSession.getConversationStatus()) {
            case AI -> {
                aiService.turnToManualJudgment(chatSession, message);
                if (chatSession.getConversationStatus() == com.smileshark.entity.Session.ConversationStatus.HUMAN) {
                    // 背AI判定为需要转人工
                    CommercialTenantEndpoint ctEndPoint = sessionFind.findCommercialTenantEndPoint(message.getSessionId());
                    if (ctEndPoint != null) {
                        // 将消息存入数据库中
                        sessionLogMapper.insert(SessionLog.builder()
                                .type(message.getType())
                                .sessionId(message.getSessionId())
                                .content(message.getMessage())
                                .build());
                        ctEndPoint.sendMessage(message);
                    }
                } else {
                    aiService.chat(chatSession, message, this);
                }
            }
            case HUMAN -> {
                // 将消息存入数据库中，只有人工的需要手动存储，这里是不需要经过redis的
                sessionLogMapper.insert(SessionLog.builder()
                        .type(message.getType())
                        .sessionId(message.getSessionId())
                        .content(message.getMessage())
                        .build());
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
            error.printStackTrace();
            session.getBasicRemote().sendObject(ChatMessage.builder()
                    .state(ChatMessage.State.ERROR)
                    .message(error.getMessage())
                    .build());
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
