package com.smileshark.controller;

import com.smileshark.entity.Goods;
import com.smileshark.service.GoodsService;
import com.smileshark.service.impl.GoodsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@SpringBootTest
public class GoodsControllerTest {
    @Autowired
    private GoodsController goodsController;

    @Test
    public void add(){
        System.out.println(goodsController.add(Goods.builder()
                .build()));
    }
}
