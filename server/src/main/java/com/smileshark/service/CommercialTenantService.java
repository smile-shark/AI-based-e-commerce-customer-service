package com.smileshark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smileshark.common.Result;
import com.smileshark.entity.CommercialTenant;

public interface CommercialTenantService extends IService<CommercialTenant> {
    Result<?> login(CommercialTenant commercialTenant);
}