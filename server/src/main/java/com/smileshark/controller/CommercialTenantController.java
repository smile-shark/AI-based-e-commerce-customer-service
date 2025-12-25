package com.smileshark.controller;

import com.smileshark.common.Result;
import com.smileshark.entity.CommercialTenant;
import com.smileshark.service.CommercialTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/commercialTenant")
@RequiredArgsConstructor
public class CommercialTenantController {
    private final CommercialTenantService commercialTenantService;
    @PostMapping("/login")
    public Result<?> login(@RequestBody CommercialTenant commercialTenant){
        return commercialTenantService.login(commercialTenant);
    }
}
