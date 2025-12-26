package com.smileshark.websocket.endpoint;

import com.smileshark.entity.SessionLog;
import com.smileshark.websocket.message.ChatMessage;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Session;

import java.io.IOException;

public interface WebSocketEndpoint {
    
    /**
     * WebSocket连接打开时的处理方法
     */
    void onOpen(Session session, Integer id);
    
    /**
     * WebSocket连接关闭时的处理方法
     */
    void onClose();
    
    /**
     * 接收到消息时的处理方法
     */
    void onMessage(ChatMessage message, Session session) throws EncodeException, IOException;
    
    /**
     * 发生错误时的处理方法
     */
    void onError(Session session, Throwable error);
    
    /**
     * 发送消息的方法
     */
    void sendMessage(ChatMessage chatMessage) throws EncodeException, IOException;
    
    /**
     * 获取会话类型
     */
    SessionLog.Type getEndpointType();
}