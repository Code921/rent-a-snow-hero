package com.morning.rentasnowhero.model.vo;

import cn.hutool.json.JSONUtil;
import com.morning.rentasnowhero.model.entity.EquipmentType;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 装备类型视图
 *
 */
@Data
public class EquipmentTypeVO implements Serializable {

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
     * @param equipmentTypeVO
     * @return
     */
    public static EquipmentType voToObj(EquipmentTypeVO equipmentTypeVO) {
        if (equipmentTypeVO == null) {
            return null;
        }
        EquipmentType equipmentType = new EquipmentType();
        BeanUtils.copyProperties(equipmentTypeVO, equipmentType);
        List<String> tagList = equipmentTypeVO.getTagList();
        equipmentType.setTags(JSONUtil.toJsonStr(tagList));
        return equipmentType;
    }

    /**
     * 对象转封装类
     *
     * @param equipmentType
     * @return
     */
    public static EquipmentTypeVO objToVo(EquipmentType equipmentType) {
        if (equipmentType == null) {
            return null;
        }
        EquipmentTypeVO equipmentTypeVO = new EquipmentTypeVO();
        BeanUtils.copyProperties(equipmentType, equipmentTypeVO);
        equipmentTypeVO.setTagList(JSONUtil.toList(equipmentType.getTags(), String.class));
        return equipmentTypeVO;
    }
}
