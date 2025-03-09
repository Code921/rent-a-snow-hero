package com.morning.rentasnowhero.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 装备表
 * @TableName equipment
 */
@TableName(value ="equipment")
@Data
public class Equipment {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long equipId;

    /**
     * 外键
     */
    private Long typeId;

    /**
     * 名称
     */
    private String name;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 图片列表
     */
    private Object imageItems;

    /**
     * 日租金
     */
    private BigDecimal dailyPrice;

    /**
     * 固定押金
     */
    private BigDecimal depositAmount;

    /**
     * 装备价格
     */
    private BigDecimal equipPrice;

    /**
     * 状态(0正常/1维修)
     */
    private Integer status;

    /**
     * 装备描述
     */
    private String description;

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