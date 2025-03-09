package com.morning.rentasnowhero.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 装备租赁日历表
 * @TableName equip_rental_calendar
 */
@TableName(value ="equip_rental_calendar")
@Data
public class EquipRentalCalendar {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long calendarId;

    /**
     * 关联装备id
     */
    private Long equipId;

    /**
     * 开始日期
     */
    private Date beginDate;

    /**
     * 结束日期
     */
    private Date endDate;

    /**
     * 逻辑删除(0正常/1删除)
     */
    private Integer isDeleted;

    /**
     * 
     */
    private Date createdTime;
}