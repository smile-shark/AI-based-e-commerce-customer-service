package com.smileshark.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smileshark.code.ResultCode;
import com.smileshark.common.Result;
import com.smileshark.entity.Role;
import com.smileshark.mapper.RoleMapper;
import com.smileshark.service.RoleService;
import com.smileshark.utils.KeyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    private final StringRedisTemplate stringRedisTemplate;
    @Value("${role.key}")
    private String key;
    @Value("${role.expiration-duration}")
    private Integer timeout;

    public RoleServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Result<?> add(Role role) {
        // 验证该商家是否已经有对应的AI客服了
        Role have = lambdaQuery().eq(Role::getCtId, role.getCtId()).one();
        if (have != null) {
            return Result.error(ResultCode.FAIL).setMessage("该商户已有AI客服");
        }
        // 保存设置的AI信息
        if (!save(role)) {
            return Result.error(ResultCode.ADD_ERROR);
        }
        return Result.success(ResultCode.ADD_SUCCESS);
    }

    @Override
    public Result<?> delete(Integer id) {
        if (!removeById(id)) {
            return Result.error(ResultCode.DELETE_ERROR);
        }
        stringRedisTemplate.delete(KeyUtils.redisKeyUtils(key, id));
        return Result.success(ResultCode.DELETE_SUCCESS);
    }

    @Override
    public Result<?> update(Role role) {
        if (!updateById(role)) {
            return Result.error(ResultCode.UPDATE_ERROR);
        }
        stringRedisTemplate.delete(KeyUtils.redisKeyUtils(key, role.getCtId()));
        return Result.success(ResultCode.UPDATE_SUCCESS);
    }

    @Override
    public Result<?> detailsByCtId(Integer ctId) {
        Role role = lambdaQuery().eq(Role::getCtId, ctId).one();
        if (role == null) {
            return Result.error(ResultCode.NOT_FOUND).setMessage("该商户没有AI客服");
        }
        return Result.success(ResultCode.SUCCESS, role);
    }

    @Override
    public Role getRoleByCtId(Integer ctId) {
        // 首先通过redis进行查询
        String json = stringRedisTemplate.opsForValue().getAndExpire(KeyUtils.redisKeyUtils(key, ctId), timeout, TimeUnit.MINUTES);
        if (json != null) {
            // 解析并返回
            return JSONUtil.toBean(json, Role.class);
        }
        // 在数据库中获取
        Role role = lambdaQuery().eq(Role::getCtId, ctId).one();
        // 存入redis中
        stringRedisTemplate.opsForValue().set(KeyUtils.redisKeyUtils(key, ctId), JSONUtil.toJsonStr(role), timeout, TimeUnit.MINUTES);
        return role;
    }

}