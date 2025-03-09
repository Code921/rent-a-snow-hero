package com.morning.rentasnowhero.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.morning.rentasnowhero.model.dto.equipmentType.EquipmentTypeQueryRequest;
import com.morning.rentasnowhero.model.entity.EquipmentType;
import com.morning.rentasnowhero.model.vo.EquipmentTypeVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 装备类型服务
 *
 */
public interface EquipmentTypeService extends IService<EquipmentType> {

    /**
     * 校验数据
     *
     * @param equipmentType
     * @param add 对创建的数据进行校验
     */
    void validEquipmentType(EquipmentType equipmentType, boolean add);

    /**
     * 获取查询条件
     *
     * @param equipmentTypeQueryRequest
     * @return
     */
    QueryWrapper<EquipmentType> getQueryWrapper(EquipmentTypeQueryRequest equipmentTypeQueryRequest);
    
    /**
     * 获取装备类型封装
     *
     * @param equipmentType
     * @param request
     * @return
     */
    EquipmentTypeVO getEquipmentTypeVO(EquipmentType equipmentType, HttpServletRequest request);

    /**
     * 分页获取装备类型封装
     *
     * @param equipmentTypePage
     * @param request
     * @return
     */
    Page<EquipmentTypeVO> getEquipmentTypeVOPage(Page<EquipmentType> equipmentTypePage, HttpServletRequest request);
}
