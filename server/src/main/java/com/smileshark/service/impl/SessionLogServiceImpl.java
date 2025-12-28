package com.smileshark.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smileshark.code.ResultCode;
import com.smileshark.common.Result;
import com.smileshark.entity.Session;
import com.smileshark.entity.SessionLog;
import com.smileshark.mapper.SessionLogMapper;
import com.smileshark.mapper.SessionMapper;
import com.smileshark.service.SessionLogService;
import com.smileshark.utils.KeyUtils;
import com.smileshark.utils.TypeConversion;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SessionLogServiceImpl extends ServiceImpl<SessionLogMapper, SessionLog> implements SessionLogService {
    private final StringRedisTemplate stringRedisTemplate;
    private final SessionMapper sessionMapper;
    @Value("${memory.redis-length}")
    private Integer memoryLength;
    @Value("${memory.expiration-duration}")
    private Integer timeout;
    @Value("${memory.key}")
    private String memoryKey;

    @Override
    @Transactional
    public void add(String sessionId, List<Message> messages) {
        // 将消息添加到mysql中
        List<SessionLog> sessionLogs = messages.stream().map(message -> {
            SessionLog.Type type = TypeConversion.messageToSessionType(message.getMessageType());
            return SessionLog.builder()
                    .sessionId(Integer.valueOf(sessionId))
                    .content(message.getText())
                    .type(type)
                    .build();
        }).toList();
        // 批量保存
        saveBatch(sessionLogs);
        // 添加到redis中
        addToRedis(sessionId, sessionLogs);
    }


    @Override
    public void addToRedis(String sessionId, List<SessionLog> sessionLogs) {
        // 添加到redis中
        stringRedisTemplate.opsForList().rightPushAll(
                KeyUtils.redisKeyUtils(memoryKey, sessionId),
                sessionLogs.stream().map(JSONUtil::toJsonStr)
                        .toList()
        );
        // 重置该key的过期时间
        stringRedisTemplate.expire(KeyUtils.redisKeyUtils(memoryKey, sessionId), timeout, TimeUnit.MINUTES);
        // 检查redis中list的长度，如果长度超过规定的长度就将旧数据删除
        if (Optional.ofNullable(stringRedisTemplate.opsForList().size(KeyUtils.redisKeyUtils(memoryKey, sessionId))).orElse(0L) > memoryLength) {
            stringRedisTemplate.opsForList().trim(KeyUtils.redisKeyUtils(memoryKey, sessionId), -memoryLength, -1);
        }
    }

    @Override
    public Result<?> readCtMessage(Integer sessionId, Integer userId) {
        // 检查是否有该会话
        Session session = sessionMapper.selectOne(new LambdaQueryWrapper<>(Session.class)
                .eq(Session::getId, sessionId)
                .eq(Session::getUserId, userId)
        );
        if (session == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        // 修改这个session的关于商户或者AI发送的消息
        lambdaUpdate().set(SessionLog::getReadStatus, SessionLog.ReadStatus.READ)
                .eq(SessionLog::getSessionId, sessionId)
                .eq(SessionLog::getType, SessionLog.Type.ASSISTANT)
                .or()
                .eq(SessionLog::getType, SessionLog.Type.COMMERCIAL_TENANT)
                .update();
        return Result.success(ResultCode.UPDATE_SUCCESS);
    }

    @Override
    public Result<?> readUserMessage(Integer sessionId, Integer ctId) {
        // 检查是否有该会话
        Session session = sessionMapper.selectOne(new LambdaQueryWrapper<>(Session.class)
                .eq(Session::getId, sessionId)
                .eq(Session::getCtId, ctId)
        );
        if (session == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        // 修改这个session的关于商户或者AI发送的消息
        lambdaUpdate().set(SessionLog::getReadStatus, SessionLog.ReadStatus.READ)
                .eq(SessionLog::getSessionId, sessionId)
                .eq(SessionLog::getType, SessionLog.Type.USER)
                .update();
        return Result.success(ResultCode.UPDATE_SUCCESS);
    }

    @Override
    public Result<List<SessionLog>> getWindowMessage(Integer sessionId) {
        return Result.success(
                ResultCode.GET_SUCCESS,
                lambdaQuery().eq(SessionLog::getSessionId, sessionId)
                        .orderByAsc(SessionLog::getTimestamp)
                        .list()
        );
    }

    @Override
    public Result<Integer> userGetUnreadMessageCount(Integer sessionId) {
        return Result.success(
                ResultCode.GET_SUCCESS,
                lambdaQuery().eq(SessionLog::getSessionId, sessionId)
                        .eq(SessionLog::getType, SessionLog.Type.COMMERCIAL_TENANT)
                        .or()
                        .eq(SessionLog::getType, SessionLog.Type.ASSISTANT)
                        .count().intValue()
        );
    }

    @Override
    public Result<Integer> ctGetUnreadMessageCount(Integer sessionId) {
        return Result.success(
                ResultCode.GET_SUCCESS,
                lambdaQuery().eq(SessionLog::getSessionId, sessionId)
                        .eq(SessionLog::getType, SessionLog.Type.USER)
                        .count().intValue()
        );
    }

    @Override
    public List<SessionLog> tryGetSessionLogs(String sessionId) {
        // 分页查询该sessionId的最近10条记录
        return lambdaQuery()
                .eq(SessionLog::getSessionId, sessionId)
                .orderByDesc(SessionLog::getTimestamp)
                .page(new Page<>(1, memoryLength))
                .getRecords();
    }
}