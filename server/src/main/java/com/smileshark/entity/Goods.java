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
 * 商品实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("goods")
public class Goods {
    /**
     * 商品id自增列
     */
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    
    /**
     * 商品名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 关联的商户
     */
    @TableField("ct_id")
    private Integer ctId;
}