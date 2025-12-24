package com.smileshark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smileshark.entity.SessionLog;
import com.smileshark.mapper.SessionLogMapper;
import com.smileshark.service.SessionLogService;
import org.springframework.stereotype.Service;

@Service
public class SessionLogServiceImpl extends ServiceImpl<SessionLogMapper, SessionLog> implements SessionLogService {
}