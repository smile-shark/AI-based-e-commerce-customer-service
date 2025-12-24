package com.smileshark.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smileshark.entity.SessionLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SessionLogMapper extends BaseMapper<SessionLog> {
}