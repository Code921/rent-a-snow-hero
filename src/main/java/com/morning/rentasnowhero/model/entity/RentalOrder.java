package com.morning.rentasnowhero.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 租赁订单表
 * @TableName rental_order
 */
@TableName(value ="rental_order")
@Data
public class RentalOrder {
    /**
     * 时间戳+随机数
     */
    @TableId
    private String orderId;

    /**
     * 外键
     */
    private Long userId;

    /**
     * 用户昵称快照
     */
    private String snapshotNickname;

    /**
     * 用户地址快照
     */
    private String snapshotAddress;

    /**
     * 使用时间
     */
    private Date useTime;

    /**
     * 租赁开始时间
     */
    private Date startTime;

    /**
     * 预计归还时间
     */
    private Date endTime;

    /**
     * 实际归还时间
     */
    private Date returnTime;

    /**
     * 总费用(租金+超时费)
     */
    private BigDecimal totalFee;

    /**
     * 押金金额
     */
    private BigDecimal depositFee;

    /**
     * 租赁日期集合
     */
    private Object rentDates;

    /**
     * 订单状态(0-6)
     */
    private Integer orderStatus;

    /**
     * 配送方式(0自提/1同城)
     */
    private Integer deliveryMethod;

    /**
     * 配送单号
     */
    private String deliveryOrderNo;

    /**
     * 装备列表
     */
    private Object equipItems;

    /**
     * 支付单号
     */
    private String paymentNo;

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