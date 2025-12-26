package com.smileshark.websocket.endpoint;

import com.smileshark.entity.SessionLog;
import com.smileshark.mapper.SessionLogMapper;
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
@ServerEndpoint("/commercialTenant/chat/{ctId}")
@RequiredArgsConstructor
public class CommercialTenantEndpoint implements WebSocketEndpoint {
    private static final ConcurrentHashMap<Integer, CommercialTenantEndpoint> commercialTenantEndpointConcurrentEndpointPool = new ConcurrentHashMap<>();
    private final SessionLogMapper sessionLogMapper;
    private final SessionFind sessionFind;

    private Session session;
    private Integer ctId;

    @OnOpen
    public void onOpen(Session session, @PathParam("ctId") Integer ctId) {
        this.session = session;
        this.ctId = ctId;
        commercialTenantEndpointConcurrentEndpointPool.put(ctId, this);
    }

    @OnClose
    public void onClose() {
        if (ctId != null) {
            commercialTenantEndpointConcurrentEndpointPool.remove(ctId);
        }
    }

    @OnMessage
    @Transactional
    public void onMessage(ChatMessage message, Session session) throws EncodeException, IOException {
        message.setType(getEndpointType());
        // 将消息存入数据库中
        sessionLogMapper.insert(SessionLog.builder()
                .type(message.getType())
                .sessionId(message.getSessionId())
                .content(message.getMessage())
                .build());
        // 通过message中的sessionId找到对应的商户会话
        UserServiceEndpoint userEndPoint = sessionFind.findUserEndPoint(message.getSessionId());
        if (userEndPoint != null) {
            userEndPoint.sendMessage(message);
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
        return SessionLog.Type.COMMERCIAL_TENANT;
    }

    public static CommercialTenantEndpoint findEndPoint(Integer userId) {
        return commercialTenantEndpointConcurrentEndpointPool.get(userId);
    }
}
