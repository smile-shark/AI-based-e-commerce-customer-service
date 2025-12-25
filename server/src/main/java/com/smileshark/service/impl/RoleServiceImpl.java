package com.smileshark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smileshark.code.ResultCode;
import com.smileshark.common.Result;
import com.smileshark.entity.Role;
import com.smileshark.mapper.RoleMapper;
import com.smileshark.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
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
        return Result.success(ResultCode.DELETE_SUCCESS);
    }

    @Override
    public Result<?> update(Role role) {
        if (!updateById(role)) {
            return Result.error(ResultCode.UPDATE_ERROR);
        }
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

}