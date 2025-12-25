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
 * 商户实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("commercial_tenant")
public class CommercialTenant {
    /**
     * 商户id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 商户名称
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