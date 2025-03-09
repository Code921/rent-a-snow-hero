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
import com.morning.rentasnowhero.model.dto.equipment.EquipmentAddRequest;
import com.morning.rentasnowhero.model.dto.equipment.EquipmentEditRequest;
import com.morning.rentasnowhero.model.dto.equipment.EquipmentQueryRequest;
import com.morning.rentasnowhero.model.dto.equipment.EquipmentUpdateRequest;
import com.morning.rentasnowhero.model.entity.Equipment;
import com.morning.rentasnowhero.model.entity.User;
import com.morning.rentasnowhero.model.vo.EquipmentVO;
import com.morning.rentasnowhero.service.EquipmentService;
import com.morning.rentasnowhero.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 装备接口
 *
 */
@RestController
@RequestMapping("/equipment")
@Slf4j
public class EquipmentController {

    @Resource
    private EquipmentService equipmentService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建装备
     *
     * @param equipmentAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addEquipment(@RequestBody EquipmentAddRequest equipmentAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(equipmentAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        Equipment equipment = new Equipment();
        BeanUtils.copyProperties(equipmentAddRequest, equipment);
        // 数据校验
        equipmentService.validEquipment(equipment, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        equipment.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = equipmentService.save(equipment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newEquipmentId = equipment.getId();
        return ResultUtils.success(newEquipmentId);
    }

    /**
     * 删除装备
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteEquipment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Equipment oldEquipment = equipmentService.getById(id);
        ThrowUtils.throwIf(oldEquipment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldEquipment.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = equipmentService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新装备（仅管理员可用）
     *
     * @param equipmentUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateEquipment(@RequestBody EquipmentUpdateRequest equipmentUpdateRequest) {
        if (equipmentUpdateRequest == null || equipmentUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Equipment equipment = new Equipment();
        BeanUtils.copyProperties(equipmentUpdateRequest, equipment);
        // 数据校验
        equipmentService.validEquipment(equipment, false);
        // 判断是否存在
        long id = equipmentUpdateRequest.getId();
        Equipment oldEquipment = equipmentService.getById(id);
        ThrowUtils.throwIf(oldEquipment == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = equipmentService.updateById(equipment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取装备（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<EquipmentVO> getEquipmentVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Equipment equipment = equipmentService.getById(id);
        ThrowUtils.throwIf(equipment == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(equipmentService.getEquipmentVO(equipment, request));
    }

    /**
     * 分页获取装备列表（仅管理员可用）
     *
     * @param equipmentQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Equipment>> listEquipmentByPage(@RequestBody EquipmentQueryRequest equipmentQueryRequest) {
        long current = equipmentQueryRequest.getCurrent();
        long size = equipmentQueryRequest.getPageSize();
        // 查询数据库
        Page<Equipment> equipmentPage = equipmentService.page(new Page<>(current, size),
                equipmentService.getQueryWrapper(equipmentQueryRequest));
        return ResultUtils.success(equipmentPage);
    }

    /**
     * 分页获取装备列表（封装类）
     *
     * @param equipmentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<EquipmentVO>> listEquipmentVOByPage(@RequestBody EquipmentQueryRequest equipmentQueryRequest,
                                                               HttpServletRequest request) {
        long current = equipmentQueryRequest.getCurrent();
        long size = equipmentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Equipment> equipmentPage = equipmentService.page(new Page<>(current, size),
                equipmentService.getQueryWrapper(equipmentQueryRequest));
        // 获取封装类
        return ResultUtils.success(equipmentService.getEquipmentVOPage(equipmentPage, request));
    }

    /**
     * 分页获取当前登录用户创建的装备列表
     *
     * @param equipmentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<EquipmentVO>> listMyEquipmentVOByPage(@RequestBody EquipmentQueryRequest equipmentQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(equipmentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        equipmentQueryRequest.setUserId(loginUser.getId());
        long current = equipmentQueryRequest.getCurrent();
        long size = equipmentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Equipment> equipmentPage = equipmentService.page(new Page<>(current, size),
                equipmentService.getQueryWrapper(equipmentQueryRequest));
        // 获取封装类
        return ResultUtils.success(equipmentService.getEquipmentVOPage(equipmentPage, request));
    }

    /**
     * 编辑装备（给用户使用）
     *
     * @param equipmentEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editEquipment(@RequestBody EquipmentEditRequest equipmentEditRequest, HttpServletRequest request) {
        if (equipmentEditRequest == null || equipmentEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Equipment equipment = new Equipment();
        BeanUtils.copyProperties(equipmentEditRequest, equipment);
        // 数据校验
        equipmentService.validEquipment(equipment, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = equipmentEditRequest.getId();
        Equipment oldEquipment = equipmentService.getById(id);
        ThrowUtils.throwIf(oldEquipment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldEquipment.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = equipmentService.updateById(equipment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
