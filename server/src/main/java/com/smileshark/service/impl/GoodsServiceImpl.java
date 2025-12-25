package com.smileshark.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smileshark.entity.Goods;
import com.smileshark.entity.GoodsDocument;
import com.smileshark.mapper.GoodsDocumentMapper;
import com.smileshark.mapper.GoodsMapper;
import com.smileshark.service.GoodsDocumentService;
import com.smileshark.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
    private final GoodsDocumentService goodsDocumentService;
    private final GoodsDocumentMapper goodsDocumentMapper;

    @Override
    @Transactional
    public int add(Goods goods) {
        save(goods);
        return goods.getId();
    }

    @Override
    @Transactional
    public int delete(Integer id) {
        // 查询到对应商品所关联的文档
        List<GoodsDocument> documents = goodsDocumentService.getListByGoodsId(id);
        // TODO 删除向量数据库中对应的文档


        return baseMapper.deleteById(id);
    }

    /**
     * 这个修改的方法只对goods表中的树做修改
     * @param goods 修改的树
     * @return 修改结果
     */
    @Override
    public Boolean update(Goods goods) {
        return updateById(goods);
    }

    /**
     * 根据id查询商品
     * @param id 商品id
     * @return 商品信息和对应的文档数据
     */
    @Override
    public Goods detailById(Integer id) {
        Goods goods = getById(id);
        // 查询对应的文档
        List<GoodsDocument> documents = goodsDocumentMapper.selectList(new LambdaQueryWrapper<>(GoodsDocument.class)
                .eq(GoodsDocument::getGoodsId, id));
        goods.setDocuments(documents);
        return goods;
    }
}