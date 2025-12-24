package com.smileshark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smileshark.entity.CommercialTenant;
import com.smileshark.mapper.CommercialTenantMapper;
import com.smileshark.service.CommercialTenantService;
import org.springframework.stereotype.Service;

@Service
public class CommercialTenantServiceImpl extends ServiceImpl<CommercialTenantMapper, CommercialTenant> implements CommercialTenantService {
}