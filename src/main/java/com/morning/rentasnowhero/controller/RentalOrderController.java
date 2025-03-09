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
import com.morning.rentasnowhero.model.dto.rentalOrder.RentalOrderAddRequest;
import com.morning.rentasnowhero.model.dto.rentalOrder.RentalOrderEditRequest;
import com.morning.rentasnowhero.model.dto.rentalOrder.RentalOrderQueryRequest;
import com.morning.rentasnowhero.model.dto.rentalOrder.RentalOrderUpdateRequest;
import com.morning.rentasnowhero.model.entity.RentalOrder;
import com.morning.rentasnowhero.model.entity.User;
import com.morning.rentasnowhero.model.vo.RentalOrderVO;
import com.morning.rentasnowhero.service.RentalOrderService;
import com.morning.rentasnowhero.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 出租订单接口
 *
 */
@RestController
@RequestMapping("/rentalOrder")
@Slf4j
public class RentalOrderController {

    @Resource
    private RentalOrderService rentalOrderService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建出租订单
     *
     * @param rentalOrderAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addRentalOrder(@RequestBody RentalOrderAddRequest rentalOrderAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(rentalOrderAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        RentalOrder rentalOrder = new RentalOrder();
        BeanUtils.copyProperties(rentalOrderAddRequest, rentalOrder);
        // 数据校验
        rentalOrderService.validRentalOrder(rentalOrder, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        rentalOrder.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = rentalOrderService.save(rentalOrder);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newRentalOrderId = rentalOrder.getId();
        return ResultUtils.success(newRentalOrderId);
    }

    /**
     * 删除出租订单
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteRentalOrder(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        RentalOrder oldRentalOrder = rentalOrderService.getById(id);
        ThrowUtils.throwIf(oldRentalOrder == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldRentalOrder.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = rentalOrderService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新出租订单（仅管理员可用）
     *
     * @param rentalOrderUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateRentalOrder(@RequestBody RentalOrderUpdateRequest rentalOrderUpdateRequest) {
        if (rentalOrderUpdateRequest == null || rentalOrderUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        RentalOrder rentalOrder = new RentalOrder();
        BeanUtils.copyProperties(rentalOrderUpdateRequest, rentalOrder);
        // 数据校验
        rentalOrderService.validRentalOrder(rentalOrder, false);
        // 判断是否存在
        long id = rentalOrderUpdateRequest.getId();
        RentalOrder oldRentalOrder = rentalOrderService.getById(id);
        ThrowUtils.throwIf(oldRentalOrder == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = rentalOrderService.updateById(rentalOrder);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取出租订单（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<RentalOrderVO> getRentalOrderVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        RentalOrder rentalOrder = rentalOrderService.getById(id);
        ThrowUtils.throwIf(rentalOrder == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(rentalOrderService.getRentalOrderVO(rentalOrder, request));
    }

    /**
     * 分页获取出租订单列表（仅管理员可用）
     *
     * @param rentalOrderQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<RentalOrder>> listRentalOrderByPage(@RequestBody RentalOrderQueryRequest rentalOrderQueryRequest) {
        long current = rentalOrderQueryRequest.getCurrent();
        long size = rentalOrderQueryRequest.getPageSize();
        // 查询数据库
        Page<RentalOrder> rentalOrderPage = rentalOrderService.page(new Page<>(current, size),
                rentalOrderService.getQueryWrapper(rentalOrderQueryRequest));
        return ResultUtils.success(rentalOrderPage);
    }

    /**
     * 分页获取出租订单列表（封装类）
     *
     * @param rentalOrderQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<RentalOrderVO>> listRentalOrderVOByPage(@RequestBody RentalOrderQueryRequest rentalOrderQueryRequest,
                                                               HttpServletRequest request) {
        long current = rentalOrderQueryRequest.getCurrent();
        long size = rentalOrderQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<RentalOrder> rentalOrderPage = rentalOrderService.page(new Page<>(current, size),
                rentalOrderService.getQueryWrapper(rentalOrderQueryRequest));
        // 获取封装类
        return ResultUtils.success(rentalOrderService.getRentalOrderVOPage(rentalOrderPage, request));
    }

    /**
     * 分页获取当前登录用户创建的出租订单列表
     *
     * @param rentalOrderQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<RentalOrderVO>> listMyRentalOrderVOByPage(@RequestBody RentalOrderQueryRequest rentalOrderQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(rentalOrderQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        rentalOrderQueryRequest.setUserId(loginUser.getId());
        long current = rentalOrderQueryRequest.getCurrent();
        long size = rentalOrderQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<RentalOrder> rentalOrderPage = rentalOrderService.page(new Page<>(current, size),
                rentalOrderService.getQueryWrapper(rentalOrderQueryRequest));
        // 获取封装类
        return ResultUtils.success(rentalOrderService.getRentalOrderVOPage(rentalOrderPage, request));
    }

    /**
     * 编辑出租订单（给用户使用）
     *
     * @param rentalOrderEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editRentalOrder(@RequestBody RentalOrderEditRequest rentalOrderEditRequest, HttpServletRequest request) {
        if (rentalOrderEditRequest == null || rentalOrderEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        RentalOrder rentalOrder = new RentalOrder();
        BeanUtils.copyProperties(rentalOrderEditRequest, rentalOrder);
        // 数据校验
        rentalOrderService.validRentalOrder(rentalOrder, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = rentalOrderEditRequest.getId();
        RentalOrder oldRentalOrder = rentalOrderService.getById(id);
        ThrowUtils.throwIf(oldRentalOrder == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldRentalOrder.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = rentalOrderService.updateById(rentalOrder);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
