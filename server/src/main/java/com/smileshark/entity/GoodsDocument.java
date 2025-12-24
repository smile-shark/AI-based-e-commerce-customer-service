package com.smileshark.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品文档实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("goods_document")
public class GoodsDocument {
    /**
     * 文档id，用户向量检索时筛选
     */
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    
    /**
     * 文档的名称，用于查看对应商品下面有哪些文档
     */
    @TableField("name")
    private String name;
    
    /**
     * 关联的商品
     */
    @TableField("goods_id")
    private Integer goodsId;
}