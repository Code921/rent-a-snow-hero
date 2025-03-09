package com.morning.rentasnowhero.model.vo;

import cn.hutool.json.JSONUtil;
import com.morning.rentasnowhero.model.entity.EquipmentRentalCalendar;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 装备出租日历表视图
 *
 */
@Data
public class EquipmentRentalCalendarVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param equipmentRentalCalendarVO
     * @return
     */
    public static EquipmentRentalCalendar voToObj(EquipmentRentalCalendarVO equipmentRentalCalendarVO) {
        if (equipmentRentalCalendarVO == null) {
            return null;
        }
        EquipmentRentalCalendar equipmentRentalCalendar = new EquipmentRentalCalendar();
        BeanUtils.copyProperties(equipmentRentalCalendarVO, equipmentRentalCalendar);
        List<String> tagList = equipmentRentalCalendarVO.getTagList();
        equipmentRentalCalendar.setTags(JSONUtil.toJsonStr(tagList));
        return equipmentRentalCalendar;
    }

    /**
     * 对象转封装类
     *
     * @param equipmentRentalCalendar
     * @return
     */
    public static EquipmentRentalCalendarVO objToVo(EquipmentRentalCalendar equipmentRentalCalendar) {
        if (equipmentRentalCalendar == null) {
            return null;
        }
        EquipmentRentalCalendarVO equipmentRentalCalendarVO = new EquipmentRentalCalendarVO();
        BeanUtils.copyProperties(equipmentRentalCalendar, equipmentRentalCalendarVO);
        equipmentRentalCalendarVO.setTagList(JSONUtil.toList(equipmentRentalCalendar.getTags(), String.class));
        return equipmentRentalCalendarVO;
    }
}
