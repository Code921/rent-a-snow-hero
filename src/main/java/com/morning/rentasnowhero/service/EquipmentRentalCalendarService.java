package com.morning.rentasnowhero.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.morning.rentasnowhero.model.dto.equipmentRentalCalendar.EquipmentRentalCalendarQueryRequest;
import com.morning.rentasnowhero.model.entity.EquipmentRentalCalendar;
import com.morning.rentasnowhero.model.vo.EquipmentRentalCalendarVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 装备出租日历表服务
 *
 */
public interface EquipmentRentalCalendarService extends IService<EquipmentRentalCalendar> {

    /**
     * 校验数据
     *
     * @param equipmentRentalCalendar
     * @param add 对创建的数据进行校验
     */
    void validEquipmentRentalCalendar(EquipmentRentalCalendar equipmentRentalCalendar, boolean add);

    /**
     * 获取查询条件
     *
     * @param equipmentRentalCalendarQueryRequest
     * @return
     */
    QueryWrapper<EquipmentRentalCalendar> getQueryWrapper(EquipmentRentalCalendarQueryRequest equipmentRentalCalendarQueryRequest);
    
    /**
     * 获取装备出租日历表封装
     *
     * @param equipmentRentalCalendar
     * @param request
     * @return
     */
    EquipmentRentalCalendarVO getEquipmentRentalCalendarVO(EquipmentRentalCalendar equipmentRentalCalendar, HttpServletRequest request);

    /**
     * 分页获取装备出租日历表封装
     *
     * @param equipmentRentalCalendarPage
     * @param request
     * @return
     */
    Page<EquipmentRentalCalendarVO> getEquipmentRentalCalendarVOPage(Page<EquipmentRentalCalendar> equipmentRentalCalendarPage, HttpServletRequest request);
}
