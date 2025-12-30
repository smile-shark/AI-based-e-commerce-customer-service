package com.smileshark.websocket.message;

import com.smileshark.entity.SessionLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    
    private Integer sessionId;
    private Integer goodsId;
    private Integer ctId;
    private SessionLog.Type type;
    private String message;
    private State state = State.SUCCESS;

    public enum State {
        SUCCESS, ERROR, SURE ,END
    }
}
