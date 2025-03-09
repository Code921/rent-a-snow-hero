package com.morning.rentasnowhero.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.morning.rentasnowhero.model.dto.equipment.EquipmentQueryRequest;
import com.morning.rentasnowhero.model.entity.Equipment;
import com.morning.rentasnowhero.model.vo.EquipmentVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 装备服务
 *
 */
public interface EquipmentService extends IService<Equipment> {

    /**
     * 校验数据
     *
     * @param equipment
     * @param add 对创建的数据进行校验
     */
    void validEquipment(Equipment equipment, boolean add);

    /**
     * 获取查询条件
     *
     * @param equipmentQueryRequest
     * @return
     */
    QueryWrapper<Equipment> getQueryWrapper(EquipmentQueryRequest equipmentQueryRequest);
    
    /**
     * 获取装备封装
     *
     * @param equipment
     * @param request
     * @return
     */
    EquipmentVO getEquipmentVO(Equipment equipment, HttpServletRequest request);

    /**
     * 分页获取装备封装
     *
     * @param equipmentPage
     * @param request
     * @return
     */
    Page<EquipmentVO> getEquipmentVOPage(Page<Equipment> equipmentPage, HttpServletRequest request);
}
