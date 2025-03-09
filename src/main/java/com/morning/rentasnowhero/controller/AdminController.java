package com.morning.rentasnowhero.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.morning.rentasnowhero.common.BaseResponse;
import com.morning.rentasnowhero.common.DeleteRequest;
import com.morning.rentasnowhero.common.ErrorCode;
import com.morning.rentasnowhero.common.ResultUtils;
import com.morning.rentasnowhero.exception.BusinessException;
import com.morning.rentasnowhero.exception.ThrowUtils;
import com.morning.rentasnowhero.model.dto.admin.AdminAddRequest;
import com.morning.rentasnowhero.model.dto.admin.AdminEditRequest;
import com.morning.rentasnowhero.model.dto.admin.AdminQueryRequest;
import com.morning.rentasnowhero.model.dto.admin.AdminUpdateRequest;
import com.morning.rentasnowhero.model.entity.Admin;
import com.morning.rentasnowhero.model.entity.User;
import com.morning.rentasnowhero.model.vo.AdminVO;
import com.morning.rentasnowhero.service.AdminService;
import com.morning.rentasnowhero.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 管理元接口
 *
 */
@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Resource
    private AdminService adminService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建管理元
     *
     * @param adminAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addAdmin(@RequestBody AdminAddRequest adminAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(adminAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        Admin admin = new Admin();
        BeanUtils.copyProperties(adminAddRequest, admin);
        // 数据校验
        adminService.validAdmin(admin, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        admin.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = adminService.save(admin);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newAdminId = admin.getId();
        return ResultUtils.success(newAdminId);
    }

    /**
     * 删除管理元
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteAdmin(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Admin oldAdmin = adminService.getById(id);
        ThrowUtils.throwIf(oldAdmin == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldAdmin.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = adminService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新管理元（仅管理员可用）
     *
     * @param adminUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAdmin(@RequestBody AdminUpdateRequest adminUpdateRequest) {
        if (adminUpdateRequest == null || adminUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Admin admin = new Admin();
        BeanUtils.copyProperties(adminUpdateRequest, admin);
        // 数据校验
        adminService.validAdmin(admin, false);
        // 判断是否存在
        long id = adminUpdateRequest.getId();
        Admin oldAdmin = adminService.getById(id);
        ThrowUtils.throwIf(oldAdmin == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = adminService.updateById(admin);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取管理元（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<AdminVO> getAdminVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Admin admin = adminService.getById(id);
        ThrowUtils.throwIf(admin == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(adminService.getAdminVO(admin, request));
    }

    /**
     * 分页获取管理元列表（仅管理员可用）
     *
     * @param adminQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Admin>> listAdminByPage(@RequestBody AdminQueryRequest adminQueryRequest) {
        long current = adminQueryRequest.getCurrent();
        long size = adminQueryRequest.getPageSize();
        // 查询数据库
        Page<Admin> adminPage = adminService.page(new Page<>(current, size),
                adminService.getQueryWrapper(adminQueryRequest));
        return ResultUtils.success(adminPage);
    }

    /**
     * 分页获取管理元列表（封装类）
     *
     * @param adminQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<AdminVO>> listAdminVOByPage(@RequestBody AdminQueryRequest adminQueryRequest,
                                                               HttpServletRequest request) {
        long current = adminQueryRequest.getCurrent();
        long size = adminQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Admin> adminPage = adminService.page(new Page<>(current, size),
                adminService.getQueryWrapper(adminQueryRequest));
        // 获取封装类
        return ResultUtils.success(adminService.getAdminVOPage(adminPage, request));
    }

    /**
     * 分页获取当前登录用户创建的管理元列表
     *
     * @param adminQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AdminVO>> listMyAdminVOByPage(@RequestBody AdminQueryRequest adminQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(adminQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        adminQueryRequest.setUserId(loginUser.getId());
        long current = adminQueryRequest.getCurrent();
        long size = adminQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Admin> adminPage = adminService.page(new Page<>(current, size),
                adminService.getQueryWrapper(adminQueryRequest));
        // 获取封装类
        return ResultUtils.success(adminService.getAdminVOPage(adminPage, request));
    }

    /**
     * 编辑管理元（给用户使用）
     *
     * @param adminEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editAdmin(@RequestBody AdminEditRequest adminEditRequest, HttpServletRequest request) {
        if (adminEditRequest == null || adminEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Admin admin = new Admin();
        BeanUtils.copyProperties(adminEditRequest, admin);
        // 数据校验
        adminService.validAdmin(admin, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = adminEditRequest.getId();
        Admin oldAdmin = adminService.getById(id);
        ThrowUtils.throwIf(oldAdmin == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldAdmin.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = adminService.updateById(admin);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
