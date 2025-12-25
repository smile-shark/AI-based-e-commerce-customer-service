package com.smileshark.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smileshark.code.ResultCode;
import com.smileshark.common.Result;
import com.smileshark.entity.Goods;
import com.smileshark.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {
    private final GoodsService goodsService;

    /**
     * 添加商品
     * @return 对应的商品id
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Goods goods) {
        return Result.success(ResultCode.ADD_SUCCESS, goodsService.add(goods));
    }

    /**
     * 删除商品
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam Integer id) {
        return Result.success(ResultCode.DELETE_SUCCESS, goodsService.delete(id));
    }

    /**
     * 修改商品数据
     * @param goods 需要修改的商品数据
     * @return 修改结果 true/false
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Goods goods) {
        return Result.success(ResultCode.UPDATE_SUCCESS, goodsService.update(goods));
    }

    /**
     * 根据id查询商品的详细数据
     * @param id 商品id
     * @return 商品数据
     */

    @GetMapping("/detailById")
    public Result<Goods> detailById(@RequestParam Integer id) {
        return Result.success(ResultCode.SUCCESS, goodsService.detailById(id));
    }

    /**
     * 分页查询商品数据
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 商品数据
     */
    @GetMapping("/pageInfo")
    public Result<Page<Goods>> pageInfo(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        return Result.success(ResultCode.SUCCESS, goodsService.page(new Page<>(pageNum, pageSize)));
    }
}
