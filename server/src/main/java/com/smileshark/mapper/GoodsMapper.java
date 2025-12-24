package com.smileshark.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smileshark.entity.Goods;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
}