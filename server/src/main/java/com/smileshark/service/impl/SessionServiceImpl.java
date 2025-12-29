package com.smileshark.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smileshark.code.ResultCode;
import com.smileshark.common.Result;
import com.smileshark.entity.Role;
import com.smileshark.entity.Session;
import com.smileshark.mapper.SessionMapper;
import com.smileshark.service.RoleService;
import com.smileshark.service.SessionService;
import com.smileshark.utils.KeyUtils;
import com.smileshark.utils.SessionFind;
import com.smileshark.websocket.endpoint.UserServiceEndpoint;
import com.smileshark.websocket.message.ChatMessage;
import jakarta.websocket.EncodeException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {
    private final RoleService roleService;
    private final StringRedisTemplate stringRedisTemplate;
    private final SessionFind sessionFind;
    @Value("${session.key}")
    private String key;
    @Value("${session.expiration-duration}")
    private Integer timeout;

    @Override
    @Transactional
    public Session find(ChatMessage message, Integer userId, UserServiceEndpoint userServiceEndpoint) throws EncodeException, IOException {
        // 首先判断是否有session
        if (!ObjectUtils.isEmpty(message.getSessionId())) {
            // 如果不是空的就直接返回
            return sessionFind.getSessionById(message.getSessionId());
        }
        // 如果是空的就查询其中是否包含对应的商品id和商户id
        if (ObjectUtils.isEmpty(message.getGoodsId()) || ObjectUtils.isEmpty(message.getCtId())) {
            throw new RuntimeException("缺失必要参数无法创建会话");
        }
        // 创建会话
        Session.SessionBuilder sessionBuilder = Session.builder()
                .ctId(message.getCtId())
                .userId(userId)
                .goodsId(message.getGoodsId());

        // 判断一下这个商户是否设置了AI客服
        Role role = roleService.getRoleByCtId(message.getCtId());
        if (role == null) {
            // 没有AI客服就设置为人工会话
            sessionBuilder.conversationStatus(Session.ConversationStatus.HUMAN);
        }else{
            // 这里需要手动设置类型，不然序列化到redis中的数据会没有这个类型
            sessionBuilder.conversationStatus(Session.ConversationStatus.AI);
        }
        // 保存会话到数据库
        Session session = sessionBuilder.build();
        save(session);
        // 保存会话到redis
        stringRedisTemplate.opsForValue().set(KeyUtils.redisKeyUtils(key, session.getId()), JSONUtil.toJsonStr(session), timeout, TimeUnit.MINUTES);
        // 将session的部分信息返回给前端
        userServiceEndpoint.sendMessage(ChatMessage.builder()
                .sessionId(session.getId())
                .state(ChatMessage.State.SURE)
                .build()
        );
        return session;
    }

    @Override
    public Result<List<Session>> userGetLastSessionList(Integer userId) {
        return Result.success(
                ResultCode.GET_SUCCESS,
                lambdaQuery()
                        .eq(Session::getUserId, userId)
                        .orderByDesc(Session::getTimestamp)
                        .list()
        );
    }

    @Override
    public Result<List<Session>> ctGetLastSessionList(Integer ctId) {
        return Result.success(
                ResultCode.GET_SUCCESS,
                lambdaQuery()
                        .eq(Session::getCtId, ctId)
                        .orderByDesc(Session::getTimestamp)
                        .list()
        );
    }
}