package com.morning.rentasnowhero.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.morning.rentasnowhero.annotation.AuthCheck;
import com.morning.rentasnowhero.common.BaseResponse;
import com.morning.rentasnowhero.common.DeleteRequest;
import com.morning.rentasnowhero.common.ErrorCode;
import com.morning.rentasnowhero.common.ResultUtils;
import com.morning.rentasnowhero.constant.UserConstant;
import com.morning.rentasnowhero.exception.BusinessException;
import com.morning.rentasnowhero.exception.ThrowUtils;
import com.morning.rentasnowhero.model.dto.cart.CartAddRequest;
import com.morning.rentasnowhero.model.dto.cart.CartEditRequest;
import com.morning.rentasnowhero.model.dto.cart.CartQueryRequest;
import com.morning.rentasnowhero.model.dto.cart.CartUpdateRequest;
import com.morning.rentasnowhero.model.entity.Cart;
import com.morning.rentasnowhero.model.entity.User;
import com.morning.rentasnowhero.model.vo.CartVO;
import com.morning.rentasnowhero.service.CartService;
import com.morning.rentasnowhero.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 租赁车接口
 *
 */
@RestController
@RequestMapping("/cart")
@Slf4j
public class CartController {

    @Resource
    private CartService cartService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建租赁车
     *
     * @param cartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addCart(@RequestBody CartAddRequest cartAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(cartAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        Cart cart = new Cart();
        BeanUtils.copyProperties(cartAddRequest, cart);
        // 数据校验
        cartService.validCart(cart, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        cart.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = cartService.save(cart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newCartId = cart.getId();
        return ResultUtils.success(newCartId);
    }

    /**
     * 删除租赁车
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteCart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Cart oldCart = cartService.getById(id);
        ThrowUtils.throwIf(oldCart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldCart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = cartService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新租赁车（仅管理员可用）
     *
     * @param cartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateCart(@RequestBody CartUpdateRequest cartUpdateRequest) {
        if (cartUpdateRequest == null || cartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Cart cart = new Cart();
        BeanUtils.copyProperties(cartUpdateRequest, cart);
        // 数据校验
        cartService.validCart(cart, false);
        // 判断是否存在
        long id = cartUpdateRequest.getId();
        Cart oldCart = cartService.getById(id);
        ThrowUtils.throwIf(oldCart == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = cartService.updateById(cart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取租赁车（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<CartVO> getCartVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Cart cart = cartService.getById(id);
        ThrowUtils.throwIf(cart == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(cartService.getCartVO(cart, request));
    }

    /**
     * 分页获取租赁车列表（仅管理员可用）
     *
     * @param cartQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Cart>> listCartByPage(@RequestBody CartQueryRequest cartQueryRequest) {
        long current = cartQueryRequest.getCurrent();
        long size = cartQueryRequest.getPageSize();
        // 查询数据库
        Page<Cart> cartPage = cartService.page(new Page<>(current, size),
                cartService.getQueryWrapper(cartQueryRequest));
        return ResultUtils.success(cartPage);
    }

    /**
     * 分页获取租赁车列表（封装类）
     *
     * @param cartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CartVO>> listCartVOByPage(@RequestBody CartQueryRequest cartQueryRequest,
                                                               HttpServletRequest request) {
        long current = cartQueryRequest.getCurrent();
        long size = cartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Cart> cartPage = cartService.page(new Page<>(current, size),
                cartService.getQueryWrapper(cartQueryRequest));
        // 获取封装类
        return ResultUtils.success(cartService.getCartVOPage(cartPage, request));
    }

    /**
     * 分页获取当前登录用户创建的租赁车列表
     *
     * @param cartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<CartVO>> listMyCartVOByPage(@RequestBody CartQueryRequest cartQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(cartQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        cartQueryRequest.setUserId(loginUser.getId());
        long current = cartQueryRequest.getCurrent();
        long size = cartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Cart> cartPage = cartService.page(new Page<>(current, size),
                cartService.getQueryWrapper(cartQueryRequest));
        // 获取封装类
        return ResultUtils.success(cartService.getCartVOPage(cartPage, request));
    }

    /**
     * 编辑租赁车（给用户使用）
     *
     * @param cartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editCart(@RequestBody CartEditRequest cartEditRequest, HttpServletRequest request) {
        if (cartEditRequest == null || cartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Cart cart = new Cart();
        BeanUtils.copyProperties(cartEditRequest, cart);
        // 数据校验
        cartService.validCart(cart, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = cartEditRequest.getId();
        Cart oldCart = cartService.getById(id);
        ThrowUtils.throwIf(oldCart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldCart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = cartService.updateById(cart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
