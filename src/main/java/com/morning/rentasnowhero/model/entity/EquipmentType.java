package com.morning.rentasnowhero.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 装备类型表
 * @TableName equipment_type
 */
@TableName(value ="equipment_type")
@Data
public class EquipmentType {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long typeId;

    /**
     * 类型名称(唯一索引)
     */
    private String typeName;

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