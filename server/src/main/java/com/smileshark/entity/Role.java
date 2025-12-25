package com.smileshark.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色实体类，用于动态控制客户角色的细微调整
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("role")
public class Role {
    /**
     * 角色的id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 角色名称
     */
    @TableField("role_name")
    private String roleName;
    
    /**
     * 角色描述
     */
    @TableField("role_description")
    private String roleDescription;
    
    /**
     * 问候语
     */
    @TableField("greeting_message")
    private String greetingMessage;
    
    /**
     * 解决问题的方法
     */
    @TableField("problem_solving_approach")
    private String problemSolvingApproach;
    
    /**
     * 沟通风格
     */
    @TableField("communication_style")
    private String communicationStyle;
    
    /**
     * 回复语调
     */
    @TableField("response_tone")
    private String responseTone;
    
    /**
     * 产品知识水平
     */
    @TableField("product_knowledge_level")
    private String productKnowledgeLevel;
    
    /**
     * 情商表现
     */
    @TableField("emotional_intelligence")
    private String emotionalIntelligence;
    
    /**
     * 升级处理标准
     */
    @TableField("escalation_criteria")
    private String escalationCriteria;
    
    /**
     * 结束语
     */
    @TableField("closing_message")
    private String closingMessage;
    
    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 关联的商户，每个商户有一个客户的角色
     */
    @TableField("ct_id")
    private Integer ctId;
}