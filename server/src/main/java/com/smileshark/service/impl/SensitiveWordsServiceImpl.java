package com.smileshark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smileshark.entity.SensitiveWords;
import com.smileshark.mapper.SensitiveWordsMapper;
import com.smileshark.service.SensitiveWordsService;
import org.springframework.stereotype.Service;

@Service
public class SensitiveWordsServiceImpl extends ServiceImpl<SensitiveWordsMapper, SensitiveWords> implements SensitiveWordsService {
}