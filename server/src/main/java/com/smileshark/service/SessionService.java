package com.smileshark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smileshark.entity.Session;
import com.smileshark.websocket.message.ChatMessage;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

public interface SessionService extends IService<Session> {

    Session find(ChatMessage message, Integer userId);

}