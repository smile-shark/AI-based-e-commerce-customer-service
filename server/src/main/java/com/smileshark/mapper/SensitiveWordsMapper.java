package com.smileshark.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smileshark.entity.SensitiveWords;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SensitiveWordsMapper extends BaseMapper<SensitiveWords> {
}