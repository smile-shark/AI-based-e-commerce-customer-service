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
 * 客户实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User {
    /**
     * 客户id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 客户名称
     */
    @TableField("name")
    private String name;

    @TableField("account")
    private String account;

    @TableField("password")
    private String password;

    public void clearPassword() {
        this.password = null;
    }
}