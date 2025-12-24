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
 * 会话记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("session_log")
public class SessionLog {
    
    /**
     * 对话人身份枚举
     */
    public enum Type {
        USER, ASSISTANT, SYSTEM, TOOL, COMMERCIAL_TENANT
    }
    
    /**
     * 记录的id
     */
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    
    /**
     * 对话的内容
     */
    @TableField("content")
    private String content;
    
    /**
     * 对话人身份
     */
    @TableField("type")
    private String type;
    
    /**
     * 记录创建的时间
     */
    @TableField("timestamp")
    private LocalDateTime timestamp;
    
    /**
     * 关联的会话
     */
    @TableField("session_id")
    private Integer sessionId;
}