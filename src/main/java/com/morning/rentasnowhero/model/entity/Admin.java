package com.morning.rentasnowhero.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 管理员表
 * @TableName admin
 */
@TableName(value ="admin")
@Data
public class Admin {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long adminId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    private String password;

    /**
     * 逻辑删除(0正常/1删除)
     */
    private Integer isDeleted;

    /**
     * 
     */
    private Date createdTime;

    /**
     * 
     */
    private Date updatedTime;
}