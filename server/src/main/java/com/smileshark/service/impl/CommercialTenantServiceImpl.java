package com.smileshark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smileshark.code.ResultCode;
import com.smileshark.common.Result;
import com.smileshark.entity.CommercialTenant;
import com.smileshark.mapper.CommercialTenantMapper;
import com.smileshark.service.CommercialTenantService;
import org.springframework.stereotype.Service;

@Service
public class CommercialTenantServiceImpl extends ServiceImpl<CommercialTenantMapper, CommercialTenant> implements CommercialTenantService {
    @Override
    public Result<?> login(CommercialTenant commercialTenant) {
        CommercialTenant account = lambdaQuery().eq(CommercialTenant::getAccount, commercialTenant.getAccount()).one();
        if (account == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
        if (!account.getPassword().equals(commercialTenant.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }
        account.clearPassword();
        return Result.success(ResultCode.LOGIN_SUCCESS, account);
    }
}