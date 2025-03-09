package com.morning.rentasnowhero.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 购物车表
 * @TableName cart
 */
@TableName(value ="cart")
@Data
public class Cart {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long cartId;

    /**
     * 外键
     */
    private Long userId;

    /**
     * 装备列表[1,2,3...]
     */
    private Object equipItems;

    /**
     * 租赁日期集合
     */
    private Object rentDates;

    /**
     * 租赁数量
     */
    private Integer quantity;

    /**
     * 日租金快照
     */
    private BigDecimal dailyPrice;

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