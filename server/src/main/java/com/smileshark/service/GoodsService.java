package com.smileshark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smileshark.entity.Goods;

public interface GoodsService extends IService<Goods> {
    int add(Goods goods);

    int delete(Integer id);

    Boolean update(Goods goods);

    Goods detailById(Integer id);
}