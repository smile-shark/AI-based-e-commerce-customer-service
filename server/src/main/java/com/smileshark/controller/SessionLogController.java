package com.smileshark.controller;

import com.smileshark.common.Result;
import com.smileshark.entity.SessionLog;
import com.smileshark.service.SessionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessionLog")
public class SessionLogController {
    private final SessionLogService sessionLogService;

    /**
     * 修改消息的状态为已读
     * @param sessionId 会话id
     * @param userId 用户id
     * @return 修改结果
     */
    @PutMapping("/readCtMessage")
    public Result<?> readCtMessage(@RequestParam("sessionId") Integer sessionId, @RequestParam("userId") Integer userId) {
        return sessionLogService.readCtMessage(sessionId, userId);
    }

    @PutMapping("/readUserMessage")
    public Result<?> readUserMessage(@RequestParam("sessionId") Integer sessionId, @RequestParam("ctId") Integer ctId) {
        return sessionLogService.readUserMessage(sessionId, ctId);
    }

    /**
     *  获取会话窗口中的消息，以时间 顺序排序
     * @param sessionId  会话id
     * @return 消息列表
     */
    @GetMapping("/getWindowMessage")
    public Result<List<SessionLog>> getWindowMessage(@RequestParam("sessionId") Integer sessionId) {
        return sessionLogService.getWindowMessage(sessionId);
    }

    /**
     * 用户获取该session的未读消息数量
     * @param sessionId  会话id
     * @return 未读消息数量
     */
    @GetMapping("/userGetUnreadMessageCount")
    public Result<Integer> userGetUnreadMessageCount(@RequestParam("sessionId") Integer sessionId) {
        return  sessionLogService.userGetUnreadMessageCount(sessionId);
    }
    /**
     * 商户获取该session的未读消息数量
     * @param sessionId  会话id
     * @return 未读消息数量
     */
    @GetMapping("/ctGetUnreadMessageCount")
    public Result<Integer> ctGetUnreadMessageCount(@RequestParam("sessionId") Integer sessionId) {
        return  sessionLogService.ctGetUnreadMessageCount(sessionId);
    }
}
