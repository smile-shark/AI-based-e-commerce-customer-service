package com.smileshark.utils;

import cn.hutool.json.JSONUtil;
import com.smileshark.entity.Session;
import com.smileshark.mapper.SessionMapper;
import com.smileshark.websocket.endpoint.CommercialTenantEndpoint;
import com.smileshark.websocket.endpoint.UserServiceEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class SessionFind {
    private final SessionMapper sessionMapper;
    @Value("${session.expiration-duration}")
    private Integer timeout;
    @Value("${session.key}")
    private Integer key;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 从Redis或带有缓存机制的数据库中获取会话数据
     * @param sessionId，要检索的会话ID
     * @return Session 对象
     */
    public Session getSessionById(Integer sessionId) {
        // 通过sessionId在redis中进行查找
        String json = stringRedisTemplate.opsForValue().getAndExpire(
                KeyUtils.redisKeyUtils(key, sessionId.toString()),
                timeout, TimeUnit.MINUTES);
        Session session;
        // 判断是否为空
        if (json == null) {
            // 在数据库中查找
            session = sessionMapper.selectById(sessionId);
            // 判断是否为空
            if (session == null) {
                throw new RuntimeException("未查询到对应的会话");
            }
            // 存入到redis中
            stringRedisTemplate.opsForValue().set(
                    KeyUtils.redisKeyUtils(key, sessionId.toString()),
                    JSONUtil.toJsonStr(session),
                    timeout, TimeUnit.MINUTES
            );
        } else {
            session = JSONUtil.toBean(json, Session.class);
        }
        return session;
    }

    public UserServiceEndpoint findUserEndPoint(Integer sessionId) {
        Session session = getSessionById(sessionId);
        // 通过session中的userId查找对应的用户的endPoint
        return UserServiceEndpoint.findEndPoint(session.getUserId());
    }

    public CommercialTenantEndpoint findCommercialTenantEndPoint(Integer sessionId) {
        Session session = getSessionById(sessionId);
        // 通过session中的ctId查找对应的商户endPoint
        return CommercialTenantEndpoint.findEndPoint(session.getCtId());
    }
}
