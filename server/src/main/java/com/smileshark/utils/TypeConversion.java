package com.smileshark.utils;

import com.smileshark.entity.SessionLog;
import org.springframework.ai.chat.messages.*;


public class TypeConversion {
    public static SessionLog.Type messageToSessionType(MessageType messageType) {
        return switch (messageType) {
            case USER -> SessionLog.Type.USER;
            case ASSISTANT -> SessionLog.Type.ASSISTANT;
            case SYSTEM -> SessionLog.Type.SYSTEM;
            case TOOL -> SessionLog.Type.TOOL;
        };
    }

    public static MessageType sessionToMessageType(SessionLog.Type sessionType) {
        return switch (sessionType) {
            case USER -> MessageType.USER;
            case ASSISTANT, COMMERCIAL_TENANT -> MessageType.ASSISTANT;
            case SYSTEM -> MessageType.SYSTEM;
            case TOOL -> MessageType.TOOL;
        };
    }
    public static Message sessionToMessage(SessionLog.Type sessionType,String content) {
        return switch (sessionType) {
            case USER -> UserMessage.builder().text(content).build();
            case ASSISTANT, COMMERCIAL_TENANT -> AssistantMessage.builder().content(content).build();
            case SYSTEM -> SystemMessage.builder().text(content).build();
            case TOOL -> null;
        };
    }
}
