package com.smileshark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smileshark.common.Result;
import com.smileshark.entity.Session;
import com.smileshark.websocket.endpoint.UserServiceEndpoint;
import com.smileshark.websocket.message.ChatMessage;
import jakarta.websocket.EncodeException;

import java.io.IOException;
import java.util.List;

public interface SessionService extends IService<Session> {

    Session find(ChatMessage message, Integer userId, UserServiceEndpoint userServiceEndpoint) throws EncodeException, IOException;

    Result<List<Session>> userGetLastSessionList(Integer userId);

    Result<List<Session>> ctGetLastSessionList(Integer ctId);
}