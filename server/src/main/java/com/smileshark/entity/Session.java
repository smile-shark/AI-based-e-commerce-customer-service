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
 * 会话实体类，一个商家和客户的会话id
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("session")
public class Session {
    /**
     * 会话id
     */
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    
    /**
     * 关联的商户
     */
    @TableField("ct_id")
    private Integer ctId;
    
    /**
     * 关联的客户
     */
    @TableField("user_id")
    private Integer userId;
}