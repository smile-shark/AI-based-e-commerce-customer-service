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
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.smileshark.code.DocumentCode.FILE_ID;
import static com.smileshark.code.DocumentCode.GOODS_ID;

@Service
@RequiredArgsConstructor
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
    private final GoodsDocumentService goodsDocumentService;
    private final GoodsDocumentMapper goodsDocumentMapper;
    private final MilvusVectorStore vectorStore;

    @Override
    @Transactional
    public int add(Goods goods) {
        save(goods);
        return goods.getId();
    }

    @Override
    @Transactional
    public int delete(Integer id) {
        // 删除向量数据库中对应的文档
        goodsDocumentMapper.delete(new LambdaQueryWrapper<>(GoodsDocument.class)
                .eq(GoodsDocument::getGoodsId, id));
        // 删除向量数据库中对应的向量
        /*
        这里可以使用两种方案，一种是通过 SpringAI提供的filterExpression来进行构建
        另一种是通过自己拼接字符串来处理
         */
        // vectorStore.delete(new Filter.Expression(Filter.ExpressionType.EQ, new Filter.Key(GOODS_ID), new Filter.Value(id)));
        vectorStore.delete(String.format("\"%s\" = %d", GOODS_ID, id));
        // 删除商品表数据
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