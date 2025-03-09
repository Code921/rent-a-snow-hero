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
import com.morning.rentasnowhero.model.dto.equipmentRentalCalendar.EquipmentRentalCalendarAddRequest;
import com.morning.rentasnowhero.model.dto.equipmentRentalCalendar.EquipmentRentalCalendarEditRequest;
import com.morning.rentasnowhero.model.dto.equipmentRentalCalendar.EquipmentRentalCalendarQueryRequest;
import com.morning.rentasnowhero.model.dto.equipmentRentalCalendar.EquipmentRentalCalendarUpdateRequest;
import com.morning.rentasnowhero.model.entity.EquipmentRentalCalendar;
import com.morning.rentasnowhero.model.entity.User;
import com.morning.rentasnowhero.model.vo.EquipmentRentalCalendarVO;
import com.morning.rentasnowhero.service.EquipmentRentalCalendarService;
import com.morning.rentasnowhero.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 装备出租日历表接口
 *
 */
@RestController
@RequestMapping("/equipmentRentalCalendar")
@Slf4j
public class EquipmentRentalCalendarController {

    @Resource
    private EquipmentRentalCalendarService equipmentRentalCalendarService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建装备出租日历表
     *
     * @param equipmentRentalCalendarAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addEquipmentRentalCalendar(@RequestBody EquipmentRentalCalendarAddRequest equipmentRentalCalendarAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(equipmentRentalCalendarAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        EquipmentRentalCalendar equipmentRentalCalendar = new EquipmentRentalCalendar();
        BeanUtils.copyProperties(equipmentRentalCalendarAddRequest, equipmentRentalCalendar);
        // 数据校验
        equipmentRentalCalendarService.validEquipmentRentalCalendar(equipmentRentalCalendar, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        equipmentRentalCalendar.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = equipmentRentalCalendarService.save(equipmentRentalCalendar);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newEquipmentRentalCalendarId = equipmentRentalCalendar.getId();
        return ResultUtils.success(newEquipmentRentalCalendarId);
    }

    /**
     * 删除装备出租日历表
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteEquipmentRentalCalendar(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        EquipmentRentalCalendar oldEquipmentRentalCalendar = equipmentRentalCalendarService.getById(id);
        ThrowUtils.throwIf(oldEquipmentRentalCalendar == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldEquipmentRentalCalendar.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = equipmentRentalCalendarService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新装备出租日历表（仅管理员可用）
     *
     * @param equipmentRentalCalendarUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateEquipmentRentalCalendar(@RequestBody EquipmentRentalCalendarUpdateRequest equipmentRentalCalendarUpdateRequest) {
        if (equipmentRentalCalendarUpdateRequest == null || equipmentRentalCalendarUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        EquipmentRentalCalendar equipmentRentalCalendar = new EquipmentRentalCalendar();
        BeanUtils.copyProperties(equipmentRentalCalendarUpdateRequest, equipmentRentalCalendar);
        // 数据校验
        equipmentRentalCalendarService.validEquipmentRentalCalendar(equipmentRentalCalendar, false);
        // 判断是否存在
        long id = equipmentRentalCalendarUpdateRequest.getId();
        EquipmentRentalCalendar oldEquipmentRentalCalendar = equipmentRentalCalendarService.getById(id);
        ThrowUtils.throwIf(oldEquipmentRentalCalendar == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = equipmentRentalCalendarService.updateById(equipmentRentalCalendar);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取装备出租日历表（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<EquipmentRentalCalendarVO> getEquipmentRentalCalendarVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        EquipmentRentalCalendar equipmentRentalCalendar = equipmentRentalCalendarService.getById(id);
        ThrowUtils.throwIf(equipmentRentalCalendar == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(equipmentRentalCalendarService.getEquipmentRentalCalendarVO(equipmentRentalCalendar, request));
    }

    /**
     * 分页获取装备出租日历表列表（仅管理员可用）
     *
     * @param equipmentRentalCalendarQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<EquipmentRentalCalendar>> listEquipmentRentalCalendarByPage(@RequestBody EquipmentRentalCalendarQueryRequest equipmentRentalCalendarQueryRequest) {
        long current = equipmentRentalCalendarQueryRequest.getCurrent();
        long size = equipmentRentalCalendarQueryRequest.getPageSize();
        // 查询数据库
        Page<EquipmentRentalCalendar> equipmentRentalCalendarPage = equipmentRentalCalendarService.page(new Page<>(current, size),
                equipmentRentalCalendarService.getQueryWrapper(equipmentRentalCalendarQueryRequest));
        return ResultUtils.success(equipmentRentalCalendarPage);
    }

    /**
     * 分页获取装备出租日历表列表（封装类）
     *
     * @param equipmentRentalCalendarQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<EquipmentRentalCalendarVO>> listEquipmentRentalCalendarVOByPage(@RequestBody EquipmentRentalCalendarQueryRequest equipmentRentalCalendarQueryRequest,
                                                               HttpServletRequest request) {
        long current = equipmentRentalCalendarQueryRequest.getCurrent();
        long size = equipmentRentalCalendarQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<EquipmentRentalCalendar> equipmentRentalCalendarPage = equipmentRentalCalendarService.page(new Page<>(current, size),
                equipmentRentalCalendarService.getQueryWrapper(equipmentRentalCalendarQueryRequest));
        // 获取封装类
        return ResultUtils.success(equipmentRentalCalendarService.getEquipmentRentalCalendarVOPage(equipmentRentalCalendarPage, request));
    }

    /**
     * 分页获取当前登录用户创建的装备出租日历表列表
     *
     * @param equipmentRentalCalendarQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<EquipmentRentalCalendarVO>> listMyEquipmentRentalCalendarVOByPage(@RequestBody EquipmentRentalCalendarQueryRequest equipmentRentalCalendarQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(equipmentRentalCalendarQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        equipmentRentalCalendarQueryRequest.setUserId(loginUser.getId());
        long current = equipmentRentalCalendarQueryRequest.getCurrent();
        long size = equipmentRentalCalendarQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<EquipmentRentalCalendar> equipmentRentalCalendarPage = equipmentRentalCalendarService.page(new Page<>(current, size),
                equipmentRentalCalendarService.getQueryWrapper(equipmentRentalCalendarQueryRequest));
        // 获取封装类
        return ResultUtils.success(equipmentRentalCalendarService.getEquipmentRentalCalendarVOPage(equipmentRentalCalendarPage, request));
    }

    /**
     * 编辑装备出租日历表（给用户使用）
     *
     * @param equipmentRentalCalendarEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editEquipmentRentalCalendar(@RequestBody EquipmentRentalCalendarEditRequest equipmentRentalCalendarEditRequest, HttpServletRequest request) {
        if (equipmentRentalCalendarEditRequest == null || equipmentRentalCalendarEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        EquipmentRentalCalendar equipmentRentalCalendar = new EquipmentRentalCalendar();
        BeanUtils.copyProperties(equipmentRentalCalendarEditRequest, equipmentRentalCalendar);
        // 数据校验
        equipmentRentalCalendarService.validEquipmentRentalCalendar(equipmentRentalCalendar, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = equipmentRentalCalendarEditRequest.getId();
        EquipmentRentalCalendar oldEquipmentRentalCalendar = equipmentRentalCalendarService.getById(id);
        ThrowUtils.throwIf(oldEquipmentRentalCalendar == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldEquipmentRentalCalendar.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = equipmentRentalCalendarService.updateById(equipmentRentalCalendar);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
