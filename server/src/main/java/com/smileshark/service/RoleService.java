package com.smileshark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smileshark.common.Result;
import com.smileshark.entity.Role;

public interface RoleService extends IService<Role> {
    Result<?> add(Role role);

    Result<?> delete(Integer id);

    Result<?> update(Role role);

    Result<?> detailsByCtId(Integer ctId);
}