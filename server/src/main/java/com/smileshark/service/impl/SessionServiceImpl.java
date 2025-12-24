package com.smileshark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smileshark.entity.Session;
import com.smileshark.mapper.SessionMapper;
import com.smileshark.service.SessionService;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {
}