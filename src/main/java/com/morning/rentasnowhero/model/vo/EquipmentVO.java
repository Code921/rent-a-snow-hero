package com.morning.rentasnowhero.model.vo;

import cn.hutool.json.JSONUtil;
import com.morning.rentasnowhero.model.entity.Equipment;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 装备视图
 *
 */
@Data
public class EquipmentVO implements Serializable {

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
     * @param equipmentVO
     * @return
     */
    public static Equipment voToObj(EquipmentVO equipmentVO) {
        if (equipmentVO == null) {
            return null;
        }
        Equipment equipment = new Equipment();
        BeanUtils.copyProperties(equipmentVO, equipment);
        List<String> tagList = equipmentVO.getTagList();
        equipment.setTags(JSONUtil.toJsonStr(tagList));
        return equipment;
    }

    /**
     * 对象转封装类
     *
     * @param equipment
     * @return
     */
    public static EquipmentVO objToVo(Equipment equipment) {
        if (equipment == null) {
            return null;
        }
        EquipmentVO equipmentVO = new EquipmentVO();
        BeanUtils.copyProperties(equipment, equipmentVO);
        equipmentVO.setTagList(JSONUtil.toList(equipment.getTags(), String.class));
        return equipmentVO;
    }
}
