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
     * 会话状态枚举
     */
    public enum ConversationStatus {
        AI, HUMAN
    }
    
    /**
     * 会话id
     */
    @TableId(type = IdType.AUTO)
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

    /**
     * 关联的商品，RAG会根据这个取查询对应商品的知识
     */
    @TableField("goods_id")
    private Integer goodsId;
    
    /**
     * 会话状态，AI表示AI对话，HUMAN表示人工对话
     */
    @TableField("conversation_status")
    private ConversationStatus conversationStatus;
}