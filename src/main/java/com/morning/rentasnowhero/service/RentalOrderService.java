package com.morning.rentasnowhero.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.morning.rentasnowhero.model.dto.rentalOrder.RentalOrderQueryRequest;
import com.morning.rentasnowhero.model.entity.RentalOrder;
import com.morning.rentasnowhero.model.vo.RentalOrderVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 出租订单服务
 *
 */
public interface RentalOrderService extends IService<RentalOrder> {

    /**
     * 校验数据
     *
     * @param rentalOrder
     * @param add 对创建的数据进行校验
     */
    void validRentalOrder(RentalOrder rentalOrder, boolean add);

    /**
     * 获取查询条件
     *
     * @param rentalOrderQueryRequest
     * @return
     */
    QueryWrapper<RentalOrder> getQueryWrapper(RentalOrderQueryRequest rentalOrderQueryRequest);
    
    /**
     * 获取出租订单封装
     *
     * @param rentalOrder
     * @param request
     * @return
     */
    RentalOrderVO getRentalOrderVO(RentalOrder rentalOrder, HttpServletRequest request);

    /**
     * 分页获取出租订单封装
     *
     * @param rentalOrderPage
     * @param request
     * @return
     */
    Page<RentalOrderVO> getRentalOrderVOPage(Page<RentalOrder> rentalOrderPage, HttpServletRequest request);
}
