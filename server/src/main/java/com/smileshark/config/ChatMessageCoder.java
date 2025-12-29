package com.smileshark.config;

import cn.hutool.json.JSONUtil;
import com.smileshark.websocket.message.ChatMessage;
import jakarta.websocket.*;

public class ChatMessageCoder implements Decoder.Text<ChatMessage>, Encoder.Text<ChatMessage> {
    @Override
    public ChatMessage decode(String s) {
        return JSONUtil.toBean(s, ChatMessage.class);
    }

    @Override
    public String encode(ChatMessage chatMessage) {
        return JSONUtil.toJsonStr(chatMessage);
    }

    @Override
    public boolean willDecode(String s) {
        return (s != null);
    }


    @Override
    public void init(EndpointConfig endpointConfig) {
        Decoder.Text.super.init(endpointConfig);
    }

    @Override
    public void destroy() {
        Decoder.Text.super.destroy();
    }
}
