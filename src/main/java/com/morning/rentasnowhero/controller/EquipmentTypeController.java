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
import com.morning.rentasnowhero.model.dto.equipmentType.EquipmentTypeAddRequest;
import com.morning.rentasnowhero.model.dto.equipmentType.EquipmentTypeEditRequest;
import com.morning.rentasnowhero.model.dto.equipmentType.EquipmentTypeQueryRequest;
import com.morning.rentasnowhero.model.dto.equipmentType.EquipmentTypeUpdateRequest;
import com.morning.rentasnowhero.model.entity.EquipmentType;
import com.morning.rentasnowhero.model.entity.User;
import com.morning.rentasnowhero.model.vo.EquipmentTypeVO;
import com.morning.rentasnowhero.service.EquipmentTypeService;
import com.morning.rentasnowhero.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 装备类型接口
 *
 */
@RestController
@RequestMapping("/equipmentType")
@Slf4j
public class EquipmentTypeController {

    @Resource
    private EquipmentTypeService equipmentTypeService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建装备类型
     *
     * @param equipmentTypeAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addEquipmentType(@RequestBody EquipmentTypeAddRequest equipmentTypeAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(equipmentTypeAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        EquipmentType equipmentType = new EquipmentType();
        BeanUtils.copyProperties(equipmentTypeAddRequest, equipmentType);
        // 数据校验
        equipmentTypeService.validEquipmentType(equipmentType, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        equipmentType.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = equipmentTypeService.save(equipmentType);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newEquipmentTypeId = equipmentType.getId();
        return ResultUtils.success(newEquipmentTypeId);
    }

    /**
     * 删除装备类型
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteEquipmentType(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        EquipmentType oldEquipmentType = equipmentTypeService.getById(id);
        ThrowUtils.throwIf(oldEquipmentType == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldEquipmentType.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = equipmentTypeService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新装备类型（仅管理员可用）
     *
     * @param equipmentTypeUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateEquipmentType(@RequestBody EquipmentTypeUpdateRequest equipmentTypeUpdateRequest) {
        if (equipmentTypeUpdateRequest == null || equipmentTypeUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        EquipmentType equipmentType = new EquipmentType();
        BeanUtils.copyProperties(equipmentTypeUpdateRequest, equipmentType);
        // 数据校验
        equipmentTypeService.validEquipmentType(equipmentType, false);
        // 判断是否存在
        long id = equipmentTypeUpdateRequest.getId();
        EquipmentType oldEquipmentType = equipmentTypeService.getById(id);
        ThrowUtils.throwIf(oldEquipmentType == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = equipmentTypeService.updateById(equipmentType);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取装备类型（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<EquipmentTypeVO> getEquipmentTypeVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        EquipmentType equipmentType = equipmentTypeService.getById(id);
        ThrowUtils.throwIf(equipmentType == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(equipmentTypeService.getEquipmentTypeVO(equipmentType, request));
    }

    /**
     * 分页获取装备类型列表（仅管理员可用）
     *
     * @param equipmentTypeQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<EquipmentType>> listEquipmentTypeByPage(@RequestBody EquipmentTypeQueryRequest equipmentTypeQueryRequest) {
        long current = equipmentTypeQueryRequest.getCurrent();
        long size = equipmentTypeQueryRequest.getPageSize();
        // 查询数据库
        Page<EquipmentType> equipmentTypePage = equipmentTypeService.page(new Page<>(current, size),
                equipmentTypeService.getQueryWrapper(equipmentTypeQueryRequest));
        return ResultUtils.success(equipmentTypePage);
    }

    /**
     * 分页获取装备类型列表（封装类）
     *
     * @param equipmentTypeQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<EquipmentTypeVO>> listEquipmentTypeVOByPage(@RequestBody EquipmentTypeQueryRequest equipmentTypeQueryRequest,
                                                               HttpServletRequest request) {
        long current = equipmentTypeQueryRequest.getCurrent();
        long size = equipmentTypeQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<EquipmentType> equipmentTypePage = equipmentTypeService.page(new Page<>(current, size),
                equipmentTypeService.getQueryWrapper(equipmentTypeQueryRequest));
        // 获取封装类
        return ResultUtils.success(equipmentTypeService.getEquipmentTypeVOPage(equipmentTypePage, request));
    }

    /**
     * 分页获取当前登录用户创建的装备类型列表
     *
     * @param equipmentTypeQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<EquipmentTypeVO>> listMyEquipmentTypeVOByPage(@RequestBody EquipmentTypeQueryRequest equipmentTypeQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(equipmentTypeQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        equipmentTypeQueryRequest.setUserId(loginUser.getId());
        long current = equipmentTypeQueryRequest.getCurrent();
        long size = equipmentTypeQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<EquipmentType> equipmentTypePage = equipmentTypeService.page(new Page<>(current, size),
                equipmentTypeService.getQueryWrapper(equipmentTypeQueryRequest));
        // 获取封装类
        return ResultUtils.success(equipmentTypeService.getEquipmentTypeVOPage(equipmentTypePage, request));
    }

    /**
     * 编辑装备类型（给用户使用）
     *
     * @param equipmentTypeEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editEquipmentType(@RequestBody EquipmentTypeEditRequest equipmentTypeEditRequest, HttpServletRequest request) {
        if (equipmentTypeEditRequest == null || equipmentTypeEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        EquipmentType equipmentType = new EquipmentType();
        BeanUtils.copyProperties(equipmentTypeEditRequest, equipmentType);
        // 数据校验
        equipmentTypeService.validEquipmentType(equipmentType, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = equipmentTypeEditRequest.getId();
        EquipmentType oldEquipmentType = equipmentTypeService.getById(id);
        ThrowUtils.throwIf(oldEquipmentType == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldEquipmentType.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = equipmentTypeService.updateById(equipmentType);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
