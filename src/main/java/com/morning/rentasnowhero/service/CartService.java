package com.morning.rentasnowhero.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.morning.rentasnowhero.model.dto.cart.CartQueryRequest;
import com.morning.rentasnowhero.model.entity.Cart;
import com.morning.rentasnowhero.model.vo.CartVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 租赁车服务
 *
 */
public interface CartService extends IService<Cart> {

    /**
     * 校验数据
     *
     * @param cart
     * @param add 对创建的数据进行校验
     */
    void validCart(Cart cart, boolean add);

    /**
     * 获取查询条件
     *
     * @param cartQueryRequest
     * @return
     */
    QueryWrapper<Cart> getQueryWrapper(CartQueryRequest cartQueryRequest);
    
    /**
     * 获取租赁车封装
     *
     * @param cart
     * @param request
     * @return
     */
    CartVO getCartVO(Cart cart, HttpServletRequest request);

    /**
     * 分页获取租赁车封装
     *
     * @param cartPage
     * @param request
     * @return
     */
    Page<CartVO> getCartVOPage(Page<Cart> cartPage, HttpServletRequest request);
}
