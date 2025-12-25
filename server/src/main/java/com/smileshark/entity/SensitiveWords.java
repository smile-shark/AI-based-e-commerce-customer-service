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
 * 敏感词实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sensitive_words")
public class SensitiveWords {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("content")
    private String content;
}