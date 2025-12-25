package com.smileshark.controller;

import com.smileshark.common.Result;
import com.smileshark.entity.User;
import com.smileshark.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user){
        return userService.login(user);
    }
}
