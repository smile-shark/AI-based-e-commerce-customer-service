package com.smileshark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smileshark.code.ResultCode;
import com.smileshark.common.Result;
import com.smileshark.entity.User;
import com.smileshark.mapper.UserMapper;
import com.smileshark.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public Result<?> login(User user) {
        User account = lambdaQuery().eq(User::getAccount, user.getAccount()).one();
        if (account == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
        if (!account.getPassword().equals(user.getPassword())) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
        account.clearPassword();
        return Result.success(ResultCode.LOGIN_SUCCESS, account);
    }

}