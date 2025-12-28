package com.smileshark.controller;

import com.smileshark.common.Result;
import com.smileshark.entity.Session;
import com.smileshark.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;

    /**
     * 用户获取到会话列表，其中会以时间倒序排序
     * @param userId 用户id
     * @return 会话列表
     */
    @GetMapping("/userGetLastSessionList")
    public Result<List<Session>> userGetLastSessionList(@RequestParam("userId")Integer userId){
        return sessionService.userGetLastSessionList(userId);
    }
    /**
     * 商户获取到会话列表，其中会以时间倒序排序
     * @param ctId 商家id
     * @return 会话列表
     */
    @GetMapping("/ctGetLastSessionList")
    public Result<List<Session>> ctGetLastSessionList(@RequestParam("ctId")Integer ctId){
        return sessionService.ctGetLastSessionList(ctId);
    }
}
