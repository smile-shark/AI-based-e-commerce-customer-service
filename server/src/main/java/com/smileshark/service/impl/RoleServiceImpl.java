package com.smileshark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smileshark.entity.Role;
import com.smileshark.mapper.RoleMapper;
import com.smileshark.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
}