package com.smileshark.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smileshark.entity.Role;
import com.smileshark.entity.Session;
import com.smileshark.mapper.RoleMapper;
import com.smileshark.mapper.SessionMapper;
import com.smileshark.service.SessionService;
import com.smileshark.utils.KeyUtils;
import com.smileshark.utils.SessionFind;
import com.smileshark.websocket.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {
    private final RoleMapper roleMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final SessionFind sessionFind;
    @Value("${session.key}")
    private String key;
    @Value("${session.expiration-duration}")
    private Integer timeout;

    @Override
    @Transactional
    public Session find(ChatMessage message, Integer userId) {
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
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<>(Role.class)
                .eq(Role::getCtId, message.getCtId()));
        if (role == null) {
            // 没有AI客服就设置为人工会话
            sessionBuilder.conversationStatus(Session.ConversationStatus.HUMAN);
        }
        // 保存会话到数据库
        Session session = sessionBuilder.build();
        save(session);
        // 保存会话到redis
        stringRedisTemplate.opsForValue().set(KeyUtils.redisKeyUtils(key, session.getId()), JSONUtil.toJsonStr(session), timeout, TimeUnit.MINUTES);
        return session;
    }
}